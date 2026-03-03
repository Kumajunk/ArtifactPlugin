package io.github.itokagimaru.artifact.auction;

import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import io.github.itokagimaru.artifact.auction.data.AuctionRepository;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.stash.StashManager;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * オークションの定期処理を管理するスケジューラークラス
 * 期限切れオークションの処理、落札、返金などを定期的に実行する。
 */
public class AuctionScheduler {

    private final JavaPlugin plugin;
    private final AuctionRepository repository;
    private final AuctionConfig config;
    private final VaultAPI vaultAPI;
    private final StashManager stashManager;
    private BukkitTask task;

    // 実行間隔（tick） 20tick = 1秒、1200tick = 1分
    private static final long CHECK_INTERVAL_TICKS = 20L;

    /**
     * コンストラクタ
     * 
     * @param plugin プラグインインスタンス
     * @param repository リポジトリ
     * @param config 設定
     * @param vaultAPI 経済API
     * @param stashManager Stashマネージャー
     */
    public AuctionScheduler(JavaPlugin plugin, AuctionRepository repository, AuctionConfig config, VaultAPI vaultAPI, StashManager stashManager) {
        this.plugin = plugin;
        this.repository = repository;
        this.config = config;
        this.vaultAPI = vaultAPI;
        this.stashManager = stashManager;
    }

    /**
     * スケジューラーを開始する
     */
    public void start() {
        if (task != null && !task.isCancelled()) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::processExpiredListings, 
            CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
        
        plugin.getLogger().info("Auction scheduler started (interval: " + 
            (CHECK_INTERVAL_TICKS / 20) + " seconds)");
    }

    /**
     * スケジューラーを停止する
     */
    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            plugin.getLogger().info("Auction scheduler stopped");
        }
    }

    /**
     * 期限切れ出品を処理する
     */
    private void processExpiredListings() {
        repository.findExpiredAsync()
                .thenAccept(expiredListings -> {
                    for (AuctionListing listing : expiredListings) {
                        try {
                            if (listing.getType() == AuctionType.AUCTION) {
                                processAuctionEnd(listing);
                            } else {
                                processExpiredBin(listing);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().severe(
                                    "Expiration processing error: " + listing.getListingId() + " - " + e.getMessage());
                        }
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to fetch expired listings: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * オークション終了を処理する
     * 
     * @param listing 期限切れのオークション出品
     */
    private void processAuctionEnd(AuctionListing listing) {
        if (listing.getCurrentBidderId() != null) {
            // 落札者がいる場合 → 落札処理
            processWin(listing);
        } else {
            // 入札がなかった場合 → 出品者にアイテム返却
            processNoSale(listing);
        }
    }

    /**
     * 落札処理を行う
     *
     * @param listing 落札されたオークション
     */
    private void processWin(AuctionListing listing) {
        long salePrice = listing.getCurrentBid();
        long saleFee = config.calculateSaleFee(salePrice);
        long sellerReceives = salePrice - saleFee;

        if (!config.isLockBidAmount()) {
            vaultAPI.withdraw(listing.getCurrentBidderId(), salePrice);
        }

        vaultAPI.deposit(listing.getSellerId(), sellerReceives);

        repository.deleteAsync(listing.getListingId())
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    stashManager.giveOrStash(listing.getCurrentBidderId(), listing.getArtifactData(), "auction_win");

                    notifyPlayer(listing.getSellerId(),
                            "§a[オークション] §fあなたのアイテムが§e$" + salePrice + "§fで落札されました（手数料: §c$" + saleFee + "§f）");
                    notifyPlayer(listing.getCurrentBidderId(),
                            "§a[オークション] §fあなたがアイテムを§e$" + salePrice + "§fで落札しました!");
                }))
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Auction win processing error: " + listing.getListingId() + " - " + ex.getMessage());
                    return null;
                });

        plugin.getLogger().info("Auction won: " + listing.getListingId()
                + " - Winner: " + listing.getCurrentBidderId() + ", Amount: $" + salePrice);
    }

    /**
     * 入札がなかった場合の処理
     *
     * @param listing 入札がなかったオークション
     */
    private void processNoSale(AuctionListing listing) {
        repository.deleteAsync(listing.getListingId())
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    stashManager.giveOrStash(listing.getSellerId(), listing.getArtifactData(), "auction_return");

                    notifyPlayer(listing.getSellerId(),
                            "§e[オークション] §fあなたの出品は入札がなく終了しました。アイテムはStashに返却されました。");
                }))
                .exceptionally(ex -> {
                    plugin.getLogger().severe("No-bid auction processing error: " + listing.getListingId() + " - " + ex.getMessage());
                    return null;
                });

        plugin.getLogger().info("Auction ended with no bids: " + listing.getListingId());
    }

    /**
     * BIN出品の期限切れ処理
     *
     * @param listing 期限切れのBIN出品
     */
    private void processExpiredBin(AuctionListing listing) {
        repository.deleteAsync(listing.getListingId())
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    stashManager.giveOrStash(listing.getSellerId(), listing.getArtifactData(), "auction_return");

                    notifyPlayer(listing.getSellerId(),
                            "§e[オークション] §fあなたの出品が期限切れになりました。アイテムはStashに返却されました。");
                }))
                .exceptionally(ex -> {
                    plugin.getLogger().severe("BIN expiration processing error: " + listing.getListingId() + " - " + ex.getMessage());
                    return null;
                });

        plugin.getLogger().info("BIN listing expired: " + listing.getListingId());
    }

    /**
     * プレイヤーにメッセージを通知する
     * 
     * @param playerId プレイヤーUUID
     * @param message メッセージ
     */
    private void notifyPlayer(UUID playerId, String message) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage(message);
        }
        // オフラインの場合は次回ログイン時にStash通知で代替
    }

}
