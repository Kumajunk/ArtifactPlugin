package io.github.itokagimaru.artifact.auction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import io.github.itokagimaru.artifact.auction.data.AuctionRepository;
import io.github.itokagimaru.artifact.auction.event.AuctionSoldEvent;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.itokagimaru.artifact.stash.StashManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * オークションのビジネスロジックを統括するマネージャークラス
 * 出品、購入、入札、キャンセルなどの全操作を管理し、
 * トランザクションの整合性を保証する。
 */
public class AuctionManager {

    private final JavaPlugin plugin;
    private final AuctionRepository repository;
    private final AuctionConfig config;
    private final VaultAPI vaultAPI;
    private final StashManager stashManager;
    private final Gson gson = new Gson();

    /**
     * コンストラクタ
     * 
     * @param plugin プラグインインスタンス
     * @param repository リポジトリ
     * @param config 設定
     * @param vaultAPI 経済API
     * @param stashManager Stashマネージャー
     */
    public AuctionManager(JavaPlugin plugin, AuctionRepository repository, AuctionConfig config, VaultAPI vaultAPI, StashManager stashManager) {
        this.plugin = plugin;
        this.repository = repository;
        this.config = config;
        this.vaultAPI = vaultAPI;
        this.stashManager = stashManager;
    }

    // ========== 出品 ==========

    /**
     * アーティファクトを出品する
     * 
     * @param seller 出品者
     * @param artifact 出品するアーティファクト
     * @param type オークション種別
     * @param price 価格（BIN）または開始価格（AUCTION）
     * @param durationHours 出品期間（時間）
     * @return 結果
     */
    public Result<AuctionListing> createListing(Player seller, BaseArtifact artifact, AuctionType type, long price, int durationHours) {
        UUID sellerId = seller.getUniqueId();

        // 最大出品数チェック
        int currentCount = repository.countBySeller(sellerId);
        if (currentCount >= config.getMaxListingsPerPlayer()) {
            return Result.failure("最大出品数（" + config.getMaxListingsPerPlayer() + "件）に達しています");
        }

        // 多重出品チェック
        Optional<AuctionListing> existing = repository.findByArtifactId(artifact.getUUID());
        if (existing.isPresent()) {
            return Result.failure("このアーティファクトは既に出品されています");
        }

        // 期間チェック
        if (durationHours > config.getMaxDurationHours()) {
            return Result.failure("最大出品期間は" + config.getMaxDurationHours() + "時間です");
        }
        if (durationHours <= 0) {
            durationHours = config.getDefaultDurationHours();
        }

        // 出品手数料計算
        long listingFee = config.calculateListingFee(price);

        // 残高チェック
        double balance = vaultAPI.getBalance(sellerId);
        if (balance < listingFee) {
            return Result.failure("出品手数料（$" + listingFee + "）を支払う残高がありません");
        }

        // アーティファクトをシリアライズ
        String artifactData = JsonConverter.serializeArtifact(artifact);

        // 出品手数料を徴収
        vaultAPI.withdraw(sellerId, listingFee);

        // 出品を作成
        long durationMillis = (long) durationHours * 60 * 60 * 1000;
        AuctionListing listing = new AuctionListing(
            sellerId, artifact.getUUID(), artifactData, type, price, durationMillis
        );

        try {
            repository.save(listing);
            plugin.getLogger().info("Listing created: " + listing.getListingId() + " by " + seller.getName());
            return Result.success(listing);
        } catch (SQLException e) {
            // 失敗時は手数料を返金
            vaultAPI.deposit(sellerId, listingFee);
            plugin.getLogger().severe("Listing save error: " + e.getMessage());
            return Result.failure("出品の保存に失敗しました");
        }
    }

    // ========== BIN購入 ==========

    /**
     * BIN出品を購入する
     * 
     * @param buyer 購入者
     * @param listingId 出品ID
     * @return 結果
     */
    public Result<Void> purchaseBin(Player buyer, UUID listingId) {
        UUID buyerId = buyer.getUniqueId();

        // 出品を取得
        Optional<AuctionListing> optListing = repository.findById(listingId);
        if (optListing.isEmpty()) {
            return Result.failure("出品が見つかりません");
        }

        AuctionListing listing = optListing.get();

        // BINかチェック
        if (listing.getType() != AuctionType.BIN) {
            return Result.failure("この出品は即時購入できません");
        }

        // 期限チェック
        if (listing.isExpired()) {
            return Result.failure("この出品は終了しています");
        }

        // 自己購入チェック
        if (listing.getSellerId().equals(buyerId)) {
            return Result.failure("自分の出品は購入できません");
        }

        // 残高チェック
        long totalPrice = listing.getPrice();
        double balance = vaultAPI.getBalance(buyerId);
        if (balance < totalPrice) {
            return Result.failure("残高が不足しています（必要: $" + totalPrice + "）");
        }

        // トランザクション開始
        try {
            // 購入者から出金
            vaultAPI.withdraw(buyerId, totalPrice);

            // 成立手数料を計算
            long saleFee = config.calculateSaleFee(totalPrice);
            long sellerReceives = totalPrice - saleFee;

            // 出品者に入金
            boolean deposited = false;
            if (Bukkit.getPlayer(listing.getSellerId()) != null) {
                deposited = vaultAPI.deposit(listing.getSellerId(), sellerReceives);
            }

            if (!deposited) {
                // オフラインまたは入金失敗時はStashへ
                String artifactName = "商品";
                try {
                    JsonObject json = gson.fromJson(listing.getArtifactData(), JsonObject.class);
                    if (json.has("seriesName")) {
                        artifactName = json.get("seriesName").getAsString();
                    }
                } catch (Exception ignored) {}
                
                boolean stashed = stashManager.stashMoney(listing.getSellerId(), sellerReceives, "オークション売上: " + artifactName);
                if (!stashed) {
                    // Stash保存も失敗した場合は深刻なエラー（ロールバックすべきだが、ここではログ出力と購入者への返金を行う）
                    throw new SQLException("売上金の保存に失敗しました");
                }
            }

            // アイテムを購入者に付与
            giveArtifactToPlayer(buyer, listing);

            // 出品を削除
            repository.delete(listingId);

            AuctionSoldEvent event = new AuctionSoldEvent(
                listing.getSellerId(),
                Objects.requireNonNull(JsonConverter.deserializeArtifact(listing.getArtifactData())).getSeriesName(),
                totalPrice
            );

            Bukkit.getServer().getPluginManager().callEvent(event);

            plugin.getLogger().info("BIN purchase completed: " + listingId + " purchased by " + buyer.getName());
            return Result.success();

        } catch (SQLException e) {
            // ロールバック（購入者への返金）
            vaultAPI.deposit(buyerId, totalPrice);
            plugin.getLogger().severe("BIN purchase error: " + e.getMessage());
            return Result.failure("購入処理に失敗しました");
        }
    }

    // ========== 入札 ==========

    /**
     * オークションに入札する
     * 
     * @param bidder 入札者
     * @param listingId 出品ID
     * @param bidAmount 入札額
     * @return 結果
     */
    public Result<Void> placeBid(Player bidder, UUID listingId, long bidAmount) {
        UUID bidderId = bidder.getUniqueId();

        // 出品を取得
        Optional<AuctionListing> optListing = repository.findById(listingId);
        if (optListing.isEmpty()) {
            return Result.failure("出品が見つかりません");
        }

        AuctionListing listing = optListing.get();

        // AUCTIONかチェック
        if (listing.getType() != AuctionType.AUCTION) {
            return Result.failure("この出品には入札できません");
        }

        // 期限チェック
        if (listing.isExpired()) {
            return Result.failure("この出品は終了しています");
        }

        // 自己入札チェック
        if (listing.getSellerId().equals(bidderId)) {
            return Result.failure("自分の出品には入札できません");
        }

        // 入札額チェック
        long minBid = listing.getCurrentBid() > 0 
            ? listing.getCurrentBid() + config.getMinBidIncrement()
            : listing.getPrice();
        
        if (bidAmount < minBid) {
            return Result.failure("入札額は$" + minBid + "以上である必要があります");
        }

        // 残高チェック
        double balance = vaultAPI.getBalance(bidderId);
        if (balance < bidAmount) {
            return Result.failure("残高が不足しています");
        }

        try {
            // 前の入札者がいれば返金
            if (listing.getCurrentBidderId() != null && config.isLockBidAmount()) {
                vaultAPI.deposit(listing.getCurrentBidderId(), listing.getCurrentBid());
            }

            // 新しい入札者から入札額をロック
            if (config.isLockBidAmount()) {
                vaultAPI.withdraw(bidderId, bidAmount);
            }

            // 入札情報を更新
            listing.updateBid(bidderId, bidAmount);
            repository.save(listing);

            // 入札履歴を記録
            repository.recordBid(listingId, bidderId, bidAmount);

            plugin.getLogger().info("Bid placed: " + listingId + " - $" + bidAmount + " by " + bidder.getName());
            return Result.success();

        } catch (SQLException e) {
            // ロールバック
            if (config.isLockBidAmount()) {
                vaultAPI.deposit(bidderId, bidAmount);
            }
            plugin.getLogger().severe("Bid error: " + e.getMessage());
            return Result.failure("入札処理に失敗しました");
        }
    }

    // ========== キャンセル ==========

    /**
     * 出品をキャンセルする
     * 
     * @param seller 出品者
     * @param listingId 出品ID
     * @return 結果
     */
    public Result<Void> cancelListing(Player seller, UUID listingId) {
        UUID sellerId = seller.getUniqueId();

        // 出品を取得
        Optional<AuctionListing> optListing = repository.findById(listingId);
        if (optListing.isEmpty()) {
            return Result.failure("出品が見つかりません");
        }

        AuctionListing listing = optListing.get();

        // 出品者チェック
        if (!listing.getSellerId().equals(sellerId)) {
            return Result.failure("この出品はキャンセルできません");
        }

        // AUCTIONの場合のみ入札がある場合はキャンセル不可
        if (listing.getType() == AuctionType.AUCTION) {
            if (listing.getCurrentBidderId() != null) {
                return Result.failure("入札があるためキャンセルできません");
            }
        }
        // BINの場合はいつでもキャンセル可能

        try {
            // 出品を削除
            repository.delete(listingId);

            // アイテムを返却
            giveArtifactToPlayer(seller, listing);

            plugin.getLogger().info("Listing cancelled: " + listingId + " by " + seller.getName());
            return Result.success();

        } catch (SQLException e) {
            plugin.getLogger().severe("Cancel error: " + e.getMessage());
            return Result.failure("キャンセル処理に失敗しました");
        }
    }

    // ========== 検索 ==========

    /**
     * 条件検索を行う
     * 
     * @param filter 検索フィルター
     * @param sortOrder ソート順
     * @param page ページ番号（0始まり）
     * @param pageSize ページサイズ
     * @return 出品リスト
     */
    public List<AuctionListing> search(AuctionSearchFilter filter, SortOrder sortOrder, int page, int pageSize) {
        int offset = page * pageSize;
        return repository.search(filter, sortOrder, pageSize, offset);
    }

    /**
     * プレイヤーの出品を取得
     * 
     * @param playerId プレイヤーUUID
     * @return 出品リスト
     */
    public List<AuctionListing> getPlayerListings(UUID playerId) {
        return repository.findBySeller(playerId);
    }

    /**
     * 出品をIDで取得
     * 
     * @param listingId 出品ID
     * @return 出品（存在しない場合はempty）
     */
    public Optional<AuctionListing> getListingById(UUID listingId) {
        return repository.findById(listingId);
    }

    // ========== アイテム付与 ==========

    /**
     * 出品からアーティファクトを復元してプレイヤーに付与する
     * 
     * @param player プレイヤー
     * @param listing 出品
     */
    public void giveArtifactToPlayer(Player player, AuctionListing listing) {
        BaseArtifact artifact = JsonConverter.deserializeArtifact(listing.getArtifactData());
        if (artifact == null) {
            player.sendMessage("§cアイテムの復元に失敗しました。管理者に連絡してください。");
            plugin.getLogger().severe("Artifact restoration failed: " + listing.getListingId());
            return;
        }

        stashManager.giveOrStash(player.getUniqueId(), listing.getArtifactData(), "オークションでの購入/返却");
    }

    /**
     * UUIDでプレイヤーにアイテムを付与（オフラインまたは満杯の場合はStashへ）
     * 
     * @param playerId プレイヤーUUID
     * @param listing 出品
     * @param source 入手元
     */
    @SuppressWarnings("unused") //コマンドによる補填用(実装予定)
    public void giveArtifactToPlayerOrStash(UUID playerId, AuctionListing listing, String source) {
        stashManager.giveOrStash(playerId, listing.getArtifactData(), source);
    }
}
