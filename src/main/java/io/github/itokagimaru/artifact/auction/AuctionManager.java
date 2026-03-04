package io.github.itokagimaru.artifact.auction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.github.itokagimaru.artifact.utils.Utils.sync;

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
     * @param seller        出品者
     * @param artifact      出品するアーティファクト
     * @param type          オークション種別
     * @param price         価格（BIN）または開始価格（AUCTION）
     * @param durationHours 出品期間（時間）
     * @return 結果
     */
    public CompletableFuture<Result<AuctionListing>> createListing(
            Player seller, BaseArtifact artifact, AuctionType type, long price, int durationHours) {

        UUID sellerId = seller.getUniqueId();
        final int finalDuration = durationHours <= 0 ? config.getDefaultDurationHours() : durationHours;

        // 1. [非同期] 出品数チェック + 多重出品チェック
        return repository.countBySellerAsync(sellerId).thenCompose(currentCount -> {
            if (currentCount >= config.getMaxListingsPerPlayer()) {
                return CompletableFuture.completedFuture(
                        Result.failure("最大出品数（" + config.getMaxListingsPerPlayer() + "件）に達しています"));
            }

            return repository.findByArtifactIdAsync(artifact.getUUID()).thenCompose(existing -> {
                if (existing.isPresent()) {
                    return CompletableFuture.completedFuture(
                            Result.failure("このアーティファクトは既に出品されています"));
                }

                // 2. [メインスレッド] 期間チェック・手数料計算・残高チェック・出金
                return CompletableFuture.supplyAsync(() -> {
                    if (finalDuration > config.getMaxDurationHours()) {
                        return Result.<AuctionListing>failure("最大出品期間は" + config.getMaxDurationHours() + "時間です");
                    }

                    long listingFee = config.calculateListingFee(price);
                    double balance = vaultAPI.getBalance(sellerId);
                    if (balance < listingFee) {
                        return Result.<AuctionListing>failure("出品手数料（$" + listingFee + "）を支払う残高がありません");
                    }

                    vaultAPI.withdraw(sellerId, listingFee);

                    long durationMillis = (long) finalDuration * 60 * 60 * 1000;
                    AuctionListing listing = new AuctionListing(
                            sellerId, artifact.getUUID(),
                            JsonConverter.serializeArtifact(artifact),
                            type, price, durationMillis
                    );
                    return Result.success(listing);
                }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable))

                // 3. [非同期] DB保存
                .thenCompose(result -> {
                    if (!result.isSuccess()) {
                        return CompletableFuture.completedFuture(result);
                    }
                    AuctionListing listing = result.getData();
                    long listingFee = config.calculateListingFee(price);
                    return repository.saveAsync(listing).thenApply(v -> {
                        plugin.getLogger().info("Listing created: " + listing.getListingId() + " by " + seller.getName());
                        return Result.success(listing);
                    }).exceptionally(ex -> {
                        // DB保存失敗時は手数料を返金
                        sync(() -> vaultAPI.deposit(sellerId, listingFee));
                        plugin.getLogger().severe("Listing save error: " + ex.getMessage());
                        return Result.failure("出品の保存に失敗しました");
                    });
                });
            });
        });
    }

    // ========== BIN購入 ==========

    public CompletableFuture<Result<Void>> purchaseBin(Player buyer, UUID listingId) {
        UUID buyerId = buyer.getUniqueId();

        // 1. [非同期] DBから出品情報を取得
        return repository.findByIdAsync(listingId).thenCompose(optListing -> {

            // 2. [メインスレッド] バリデーションと出金処理
            // ※ CompletableFuture.supplyAsync を使ってメインスレッドへ切り替え
            return CompletableFuture.supplyAsync(() -> {
                if (optListing.isEmpty()) return Result.<AuctionListing>failure("出品が見つかりません");

                AuctionListing listing = optListing.get();
                // 基本チェック
                if (listing.getType() != AuctionType.BIN) return Result.<AuctionListing>failure("この出品は即時購入できません");
                if (listing.isExpired()) return Result.<AuctionListing>failure("この出品は終了しています");
                if (listing.getSellerId().equals(buyerId)) return Result.<AuctionListing>failure("自分の出品は購入できません");

                // 残高チェック
                long totalPrice = listing.getPrice();
                if (vaultAPI.getBalance(buyerId) < totalPrice) {
                    return Result.<AuctionListing>failure("残高が不足しています（必要: $" + totalPrice + "）");
                }

                // 【重要】ここで一旦出金（仮押さえ）
                if (!vaultAPI.withdraw(buyerId, totalPrice)) {
                    return Result.<AuctionListing>failure("出金処理に失敗しました");
                }

                return Result.success(listing);
            }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));

        }).thenCompose(result -> {
            // バリデーション失敗ならそのまま返す
            if (!result.isSuccess()) return CompletableFuture.completedFuture(Result.failure(result.getErrorMessage()));

            AuctionListing listing = result.getData();
            long totalPrice = listing.getPrice();

            // 3. [非同期] DBから出品を削除（これが売買の確定フラグ！）
            return repository.deleteAsync(listingId).thenApply(v -> {
                // 削除成功：売買成立
                processPostPurchase(buyer, listing);
                return Result.<Void>success();

            }).exceptionally(ex -> {
                // 削除失敗：DBエラー。返金処理（メインスレッドに戻す）
                sync(() -> {
                    vaultAPI.deposit(buyerId, totalPrice);
                    buyer.sendMessage("§c購入処理に失敗しました。返金されました。");
                });
                return Result.failure("データベースエラーにより購入に失敗しました");
            });
        });
    }

    /**
     * 購入成立後の事後処理（メインスレッド）
     */
    private void processPostPurchase(Player buyer, AuctionListing listing) {
        sync(() -> {
            long totalPrice = listing.getPrice();
            long saleFee = config.calculateSaleFee(totalPrice);
            long sellerReceives = totalPrice - saleFee;

            // 出品者への入金処理（オンラインなら即座に、オフラインならStashへ）
            boolean deposited = vaultAPI.deposit(listing.getSellerId(), sellerReceives);

            if (!deposited) {
                String artifactName = getArtifactName(listing);
                stashManager.stashMoney(listing.getSellerId(), sellerReceives, "オークション売上: " + artifactName);
            }

            // 購入者にアイテム付与
            giveArtifactToPlayer(buyer, listing);

            // イベント呼び出し
            AuctionSoldEvent event = new AuctionSoldEvent(
                    listing.getSellerId(),
                    getArtifactName(listing),
                    totalPrice
            );
            Bukkit.getServer().getPluginManager().callEvent(event);

            buyer.sendMessage("§a購入が完了しました!");
        });
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
    public CompletableFuture<Result<?>> placeBid(Player bidder, UUID listingId, long bidAmount) {
        UUID bidderId = bidder.getUniqueId();

        // 1. [非同期: 仮想スレッド] DBから最新の出品データを取得
        return repository.findByIdAsync(listingId).thenCompose(optListing -> {

            // 2. [メインスレッド] バリデーションとお金の仮押さえ
            // Bukkit API (所持金確認・出金) を叩くためにメインスレッドに切り替え
            return CompletableFuture.supplyAsync(() -> {
                if (optListing.isEmpty()) {
                    return Result.<AuctionListing>failure("出品が見つかりません");
                }

                AuctionListing listing = optListing.get();

                if (listing.getType() != AuctionType.AUCTION) {
                    return Result.<AuctionListing>failure("この出品はオークション形式ではありません");
                }
                if (listing.isExpired()) {
                    return Result.<AuctionListing>failure("この出品は既に終了しています");
                }
                if (listing.getSellerId().equals(bidderId)) {
                    return Result.<AuctionListing>failure("自分の出品には入札できません");
                }

                // 最低入札額の計算
                long minBid = listing.getCurrentBid() > 0
                        ? listing.getCurrentBid() + config.getMinBidIncrement()
                        : listing.getPrice();

                if (bidAmount < minBid) {
                    return Result.<AuctionListing>failure("入札額は$" + minBid + "以上必要です");
                }

                // 所持金チェックと出金 (仮押さえ)
                if (vaultAPI.getBalance(bidderId) < bidAmount) {
                    return Result.<AuctionListing>failure("残高が不足しています");
                }

                if (config.isLockBidAmount()) {
                    if (!vaultAPI.withdraw(bidderId, bidAmount)) {
                        return Result.<AuctionListing>failure("出金処理に失敗しました");
                    }
                }

                return Result.success(listing);
            }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));

        }).thenCompose(result -> {
            // バリデーション失敗時はここで終了
            if (!result.isSuccess()) {
                return CompletableFuture.completedFuture(Result.failure(result.getErrorMessage()));
            }

            AuctionListing listing = result.getData();
            long oldAmount = listing.getCurrentBid();
            UUID oldBidderId = listing.getCurrentBidderId();

            // 3. [非同期: 仮想スレッド] DBをアトミックに更新
            // Repository側の updateBidAtomicAsync を使用して「競合」をチェック
            return repository.updateBidAtomicAsync(listingId, bidderId, bidAmount, oldAmount).thenApply(success -> {

                // 4. [メインスレッド] 更新結果に基づいた最終処理
                sync(() -> {
                    if (success) {
                        // 【成功】前の入札者に返金し、入札履歴を保存
                        if (oldBidderId != null && config.isLockBidAmount()) {
                            vaultAPI.deposit(oldBidderId, oldAmount);
                        }
                        repository.recordBidAsync(listingId, bidderId, bidAmount)
                                .exceptionally(ex -> {
                                    plugin.getLogger().severe("入札履歴の保存に失敗しました: "+ex.getMessage());
                                    return  null;
                                });
                        bidder.sendMessage("§a§l[!] §f$" + bidAmount + " で入札しました！");

                        plugin.getLogger().info("Bid confirmed: " + listingId + " by " + bidder.getName());
                    } else {
                        // 【失敗】他の人が先に入札したため、今回引いたお金を返金（ロールバック）
                        if (config.isLockBidAmount()) {
                            vaultAPI.deposit(bidderId, bidAmount);
                        }
                        bidder.sendMessage("§c§l[!] §c処理中に他のプレイヤーが入札したため、失敗しました。");
                    }
                });

                return success ? Result.<Void>success() : Result.<Void>failure("入札が競合しました");
            });

        }).exceptionally(ex -> {
            // 5. [例外処理] 予期せぬエラー発生時のロールバック
            plugin.getLogger().severe("入札処理中にエラーが発生しました: " + ex.getMessage());
            sync(() -> bidder.sendMessage("§c§l[!] §cシステムエラーにより入札できませんでした。"));
            return Result.failure("エラーが発生しました");
        });
    }

    // ========== キャンセル ==========

    /**
     * 出品をキャンセルする
     *
     * @param seller    出品者
     * @param listingId 出品ID
     * @return 結果
     */
    public CompletableFuture<Result<Void>> cancelListing(Player seller, UUID listingId) {
        UUID sellerId = seller.getUniqueId();

        return repository.findByIdAsync(listingId)
                .thenCompose(optListing -> CompletableFuture.supplyAsync(() -> {
                    if (optListing.isEmpty()) {
                        return Result.<AuctionListing>failure("出品が見つかりません");
                    }

                    AuctionListing listing = optListing.get();

                    if (!listing.getSellerId().equals(sellerId)) {
                        return Result.<AuctionListing>failure("この出品はキャンセルできません");
                    }

                    if (listing.getType() == AuctionType.AUCTION && listing.getCurrentBidderId() != null) {
                        return Result.<AuctionListing>failure("入札があるためキャンセルできません");
                    }

                    // アイテムを返却
                    giveArtifactToPlayer(seller, listing);
                    return Result.success(listing);

                }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable)))

                .thenCompose(result -> {
                    if (!result.isSuccess()) {
                        return CompletableFuture.completedFuture(Result.failure(result.getErrorMessage()));
                    }

                    return repository.deleteAsync(listingId).thenApply(v -> {
                        plugin.getLogger().info("Listing cancelled: " + listingId + " by " + seller.getName());
                        return Result.<Void>success();
                    }).exceptionally(ex -> {
                        plugin.getLogger().severe("出品の削除中にエラーが発生しました: " + ex.getMessage());
                        return Result.failure("データベースエラーによりキャンセルに失敗しました");
                    });
                });
    }

    // ========== 検索 ==========

    /**
     * 条件検索を行う
     *
     * @param filter    検索フィルター
     * @param sortOrder ソート順
     * @param page      ページ番号（0始まり）
     * @param pageSize  ページサイズ
     * @return 出品リスト
     */
    public CompletableFuture<List<AuctionListing>> search(AuctionSearchFilter filter, SortOrder sortOrder, int page, int pageSize) {
        int offset = page * pageSize;
        return repository.searchAsync(filter, sortOrder, pageSize, offset);
    }

    /**
     * プレイヤーの出品を取得
     *
     * @param playerId プレイヤーUUID
     * @return 出品リスト
     */
    public CompletableFuture<List<AuctionListing>> getPlayerListings(UUID playerId) {
        return repository.findBySellerAsync(playerId);
    }

    /**
     * 出品をIDで取得
     *
     * @param listingId 出品ID
     * @return 出品（存在しない場合はempty）
     */
    public CompletableFuture<Optional<AuctionListing>> getListingById(UUID listingId) {
        return repository.findByIdAsync(listingId);
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

    public List<String> getAllowedWorlds() {
        return config.getAllowedWorlds();
    }

    public AuctionConfig getConfig() {
        return config;
    }

    private String getArtifactName(AuctionListing listing) {
        try {
            // artifactDataはJSON文字列なので、GsonでパースしてseriesNameを抜く
            JsonObject json = gson.fromJson(listing.getArtifactData(), JsonObject.class);
            if (json.has("seriesName")) {
                return json.get("seriesName").getAsString();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse artifact data: " + listing.getArtifactData());
        }
        return "不明なアイテム";
    }
}
