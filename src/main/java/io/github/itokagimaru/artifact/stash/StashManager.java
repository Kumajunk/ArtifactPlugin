package io.github.itokagimaru.artifact.stash;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.github.itokagimaru.artifact.artifact.JsonConverter.deserializeArtifact;
import static io.github.itokagimaru.artifact.utils.Utils.sync;

/**
 * InventoryStashのビジネスロジックを管理するクラス
 * プレイヤーがオフラインまたはインベントリ満杯の場合に
 * アイテムを一時保管し、後で取り出せるようにする。
 */
public class StashManager {

    private final JavaPlugin plugin;
    private final StashRepository repository;
    private final VaultAPI vaultAPI;
    private final Gson gson = new Gson();

    public StashManager(JavaPlugin plugin, StashRepository repository, VaultAPI vaultAPI) {
        this.plugin = plugin;
        this.repository = repository;
        this.vaultAPI = vaultAPI;
    }

    /**
     * アーティファクトをプレイヤーに付与する（オフライン/満杯の場合はStashへ）
     *
     * @param playerId     プレイヤーUUID
     * @param artifactData シリアライズされたアーティファクトデータ
     * @param source       アイテムの入手元
     */
    public void giveOrStash(UUID playerId, String artifactData, String source) {
        Player player = Bukkit.getPlayer(playerId);
        
        if (player == null || !player.isOnline()) {
            // オフラインの場合はStashに保存
            stashItem(playerId, artifactData, source);
            return;
        }

        // アーティファクトを復元
        BaseArtifact artifact = JsonConverter.deserializeArtifact(artifactData);
        if (artifact == null) {
            plugin.getLogger().severe("アーティファクト復元失敗: " + playerId);
            // 復元失敗でもStashに保存して後で再試行可能にする
            stashItem(playerId, artifactData, source);
            return;
        }

        // ItemStackを作成
        ItemStack item = ArtifactToItem.convert(artifact);

        // インベントリに追加を試みる
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);

        if (!overflow.isEmpty()) {
            // 満杯の場合はStashに保存
            for (ItemStack ignored : overflow.values()) {
                // オーバーフローしたアイテムをStashへ
                stashItem(playerId, artifactData, source);
            }
            player.sendMessage("§e[Stash] §fインベントリが満杯のため、アイテムをStashに保管しました");
            player.sendMessage("§7/stash で取り出せます");
        }

    }

    /**
     * アイテムをStashに保存する
     */
    public CompletableFuture<Boolean> stashItem(UUID playerId, String itemData, String source) {
        return CompletableFuture.supplyAsync(() -> {
            StashItem item = new StashItem(playerId, itemData, source);
            repository.saveAsync(item).thenAccept(success -> sync(() -> plugin.getLogger().info("Stash保存: " + playerId + " - " + source)));
            return true;
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Stash保存エラー: " + ex.getMessage());
            return false;
        });
    }

    /**
     * お金をStashに保存する
     */
    public void stashMoney(UUID playerId, double amount, String source) {
        JsonObject json = new JsonObject();
        json.addProperty("isMoney", true);
        json.addProperty("amount", amount);

        stashItem(playerId, gson.toJson(json), source);
    }

    /**
     * プレイヤーのStashアイテム一覧を取得
     */
    public CompletableFuture<List<StashItem>> getPlayerStash(UUID playerId) {
        return repository.findByPlayerAsync(playerId);
    }

    /**
     * プレイヤーのStashアイテム数を取得
     */
    public CompletableFuture<Integer> getStashCount(UUID playerId) {
        return repository.countByPlayerAsync(playerId);
    }

    /**
     * Stashからアイテムを取り出す
     *
     * @param player プレイヤー
     * @param itemId アイテムID
     * @return 成功した場合true
     */
    public CompletableFuture<Boolean> withdrawItem(Player player, UUID itemId) {
        // 1. [非同期] DBからアイテムを取得
        return repository.findByIdAsync(itemId).thenCompose(stashItem -> {
            if (stashItem == null) {
                sync(() -> player.sendMessage("§cアイテムが見つかりません"));
                return CompletableFuture.completedFuture(false);
            }

            // 2. [メインスレッド] 所有権の検証とアイテム付与
            return CompletableFuture.<Optional<Boolean>>supplyAsync(() -> {
                if (!stashItem.getPlayerId().equals(player.getUniqueId())) {
                    player.sendMessage("§cこのアイテムを取り出す権限がありません");
                    return Optional.of(false);
                }

                // 通貨データかどうかをチェック
                try {
                    JsonObject json = gson.fromJson(stashItem.getItemData(), JsonObject.class);
                    if (json.has("isMoney") && json.get("isMoney").getAsBoolean()) {
                        double amount = json.get("amount").getAsDouble();
                        boolean deposited = vaultAPI.deposit(player.getUniqueId(), amount);
                        if (deposited) {
                            String formatted = NumberFormat.getNumberInstance(Locale.US).format(amount);
                            player.sendMessage("§a[Stash] §e$" + formatted + " §aを受け取りました");
                            return Optional.of(true); // 成功シグナル → DBから削除へ
                        } else {
                            player.sendMessage("§c入金処理に失敗しました");
                            return Optional.of(false);
                        }
                    }
                } catch (Exception ignored) {
                    // アーティファクトとして処理を継続。
                }

                BaseArtifact artifact = deserializeArtifact(stashItem.getItemData());
                if (artifact == null) {
                    player.sendMessage("§cアイテムの復元に失敗しました");
                    return Optional.empty(); // エラー: DBからは削除しない
                }

                ItemStack item = ArtifactToItem.convert(artifact);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);

                if (!overflow.isEmpty()) {
                    player.sendMessage("§cインベントリに空きがありません");
                    for (ItemStack returnItem : overflow.values()) {
                        player.getInventory().removeItem(returnItem);
                    }
                    return Optional.of(false);
                }

                player.sendMessage("§aアイテムを取り出しました");
                return Optional.of(true);

            }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable))
            .thenCompose(result -> {
                if (result.isEmpty() || !result.get()) {
                    return CompletableFuture.completedFuture(false);
                }
                // 3. [非同期] DBから削除
                return repository.deleteAsync(itemId).thenApply(v -> true)
                        .exceptionally(ex -> {
                            plugin.getLogger().severe("Stash削除に失敗しました: " + ex.getMessage());
                            return false;
                        });
            });
        });
    }

    /**
     * プレイヤーのStashから全てのアイテムを取り出します。
     * メインスレッドから呼び出す必要があります。
     *
     * @param player プレイヤー
     * @return 正常に取り出されたアイテム数で解決される CompletableFuture
     */
    public CompletableFuture<Integer> withdrawAll(Player player) {
        return repository.findByPlayerAsync(player.getUniqueId()).thenCompose(items -> {
            // CompletableFuture を連鎖させて順次処理を行う
            CompletableFuture<Integer> chain = CompletableFuture.completedFuture(0);
            for (StashItem stashItem : items) {
                chain = chain.thenCompose(count ->
                        withdrawItem(player, stashItem.getId()).thenApply(success -> {
                            if (success) return count + 1;
                            // インベントリが満杯の場合は以降の処理を中断
                            throw new RuntimeException("inventory_full");
                        })
                );
            }
            return chain.exceptionally(ex -> {
                if (ex.getCause() != null && "inventory_full".equals(ex.getCause().getMessage())) {
                    // 失敗するまでに回収できた個数を返す（現在の設計では0で丸めているが、要件に応じ調整可能）
                    return 0;
                }
                plugin.getLogger().severe("withdrawAll error: " + ex.getMessage());
                return 0;
            });
        });
    }
}
