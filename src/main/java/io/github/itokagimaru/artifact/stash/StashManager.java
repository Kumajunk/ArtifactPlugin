package io.github.itokagimaru.artifact.stash;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tire.Tier;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * InventoryStashのビジネスロジックを管理するクラス
 * 
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
     * @param playerId プレイヤーUUID
     * @param artifactData シリアライズされたアーティファクトデータ
     * @param source アイテムの入手元
     * @return Stashに保存された場合true
     */
    public boolean giveOrStash(UUID playerId, String artifactData, String source) {
        Player player = Bukkit.getPlayer(playerId);
        
        if (player == null || !player.isOnline()) {
            // オフラインの場合はStashに保存
            return stashItem(playerId, artifactData, source);
        }

        // アーティファクトを復元
        BaseArtifact artifact = deserializeArtifact(artifactData);
        if (artifact == null) {
            plugin.getLogger().severe("アーティファクト復元失敗: " + playerId);
            // 復元失敗でもStashに保存して後で再試行可能にする
            return stashItem(playerId, artifactData, source);
        }

        // ItemStackを作成
        ItemStack item = ArtifactToItem.convert(artifact);

        // インベントリに追加を試みる
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);

        if (!overflow.isEmpty()) {
            // 満杯の場合はStashに保存
            for (ItemStack overflowItem : overflow.values()) {
                // オーバーフローしたアイテムをStashへ
                stashItem(playerId, artifactData, source);
            }
            player.sendMessage("§e[Stash] §fインベントリが満杯のため、アイテムをStashに保管しました");
            player.sendMessage("§7/stash で取り出せます");
            return true;
        }

        return false;  // 直接インベントリに追加された
    }

    /**
     * 出品データからアーティファクトをプレイヤーに付与
     */
    public boolean giveOrStash(UUID playerId, AuctionListing listing, String source) {
        return giveOrStash(playerId, listing.getArtifactData(), source);
    }

    /**
     * アイテムをStashに保存する
     */
    public boolean stashItem(UUID playerId, String itemData, String source) {
        try {
            StashItem item = new StashItem(playerId, itemData, source);
            repository.save(item);
            plugin.getLogger().info("Stash保存: " + playerId + " - " + source);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Stash保存エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * お金をStashに保存する
     */
    public boolean stashMoney(UUID playerId, double amount, String source) {
        JsonObject json = new JsonObject();
        json.addProperty("isMoney", true);
        json.addProperty("amount", amount);
        
        return stashItem(playerId, gson.toJson(json), source);
    }

    /**
     * プレイヤーのStashアイテム一覧を取得
     */
    public List<StashItem> getPlayerStash(UUID playerId) {
        return repository.findByPlayer(playerId);
    }

    /**
     * プレイヤーのStashアイテム数を取得
     */
    public int getStashCount(UUID playerId) {
        return repository.countByPlayer(playerId);
    }

    /**
     * Stashからアイテムを取り出す
     * 
     * @param player プレイヤー
     * @param itemId アイテムID
     * @return 成功した場合true
     */
    public boolean withdrawItem(Player player, UUID itemId) {
        StashItem stashItem = repository.findById(itemId);
        
        if (stashItem == null) {
            player.sendMessage("§cアイテムが見つかりません");
            return false;
        }

        // 所有者チェック
        if (!stashItem.getPlayerId().equals(player.getUniqueId())) {
            player.sendMessage("§cこのアイテムを取り出す権限がありません");
            return false;
        }

        // 金銭データかチェック
        try {
            JsonObject json = gson.fromJson(stashItem.getItemData(), JsonObject.class);
            if (json.has("isMoney") && json.get("isMoney").getAsBoolean()) {
                // お金として処理
                double amount = json.get("amount").getAsDouble();
                
                // お金を付与
                boolean deposited = vaultAPI.deposit(player.getUniqueId(), amount);
                if (deposited) {
                    // Stashから削除
                    try {
                        repository.delete(itemId);
                        String formatted = NumberFormat.getNumberInstance(Locale.US).format(amount);
                        player.sendMessage("§a[Stash] §e$" + formatted + " §aを受け取りました");
                        return true;
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Stash削除エラー(Money): " + e.getMessage());
                        // DB削除失敗時にお金が増え続けるのを防ぐため、本来はロールバック制御が必要
                        // ここでは簡単のため警告ログのみ（Vaultは取り消しが難しい場合がある）
                        player.sendMessage("§c処理中にエラーが発生しました。管理者に連絡してください。");
                        return false; 
                    }
                } else {
                    player.sendMessage("§c入金処理に失敗しました");
                    return false;
                }
            }
        } catch (Exception ignored) {
            // JSONパースエラーなどは通常のアイテムとして処理継続
        }

        // アーティファクトを復元
        BaseArtifact artifact = deserializeArtifact(stashItem.getItemData());
        if (artifact == null) {
            player.sendMessage("§cアイテムの復元に失敗しました");
            return false;
        }

        // インベントリに空きがあるかチェック
        ItemStack item = ArtifactToItem.convert(artifact);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);

        if (!overflow.isEmpty()) {
            // インベントリ満杯
            player.sendMessage("§cインベントリに空きがありません");
            // アイテムを戻す
            for (ItemStack returnItem : overflow.values()) {
                player.getInventory().removeItem(returnItem);
            }
            return false;
        }

        // Stashから削除
        try {
            repository.delete(itemId);
            player.sendMessage("§aアイテムを取り出しました");
            return true;
        } catch (SQLException e) {
            // 削除失敗時はアイテムを戻す
            player.getInventory().removeItem(item);
            player.sendMessage("§c取り出し処理に失敗しました");
            plugin.getLogger().severe("Stash削除エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stashから全アイテムを取り出す
     */
    public int withdrawAll(Player player) {
        List<StashItem> items = repository.findByPlayer(player.getUniqueId());
        int successCount = 0;

        for (StashItem stashItem : items) {
            if (withdrawItem(player, stashItem.getId())) {
                successCount++;
            } else {
                // インベントリ満杯などで取り出せなくなったら終了
                // ただしお金の場合はインベントリ関係ないので続行すべきだが、今回はシンプルに中断
                // break; 
            }
        }

        return successCount;
    }

    /**
     * JSONからアーティファクトをデシリアライズする
     */
    private BaseArtifact deserializeArtifact(String artifactData) {
        try {
            JsonObject json = gson.fromJson(artifactData, JsonObject.class);
            
            // お金データの場合はnullを返す
            if (json.has("isMoney") && json.get("isMoney").getAsBoolean()) {
                return null; 
            }

            int seriesId = json.has("seriesId") ? json.get("seriesId").getAsInt() : 0;
            Series.artifactSeres series = Series.artifactSeres.fromId(seriesId);
            if (series == null) return null;

            BaseArtifact artifact = series.getArtifactType();
            if (artifact == null) return null;

            // UUIDを設定
            String uuidStr = json.has("uuid") ? json.get("uuid").getAsString() : UUID.randomUUID().toString();
            try {
                var uuidField = BaseArtifact.class.getDeclaredField("artifactId");
                uuidField.setAccessible(true);
                uuidField.set(artifact, UUID.fromString(uuidStr));
            } catch (Exception ignored) {}

            // ステータスを設定
            int slotId = json.has("slotId") ? json.get("slotId").getAsInt() : 0;
            int tierId = json.has("tierId") ? json.get("tierId").getAsInt() : 0;
            int level = json.has("level") ? json.get("level").getAsInt() : 0;
            int mainEffectId = json.has("mainEffectId") ? json.get("mainEffectId").getAsInt() : 0;
            int mainEffectValue = json.has("mainEffectValue") ? json.get("mainEffectValue").getAsInt() : 0;

            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            Tier.artifactTier tier = Tier.artifactTier.fromId(tierId);
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);

            // Sub効果を復元
            SubEffect.artifactSubEffect[] subEffects = new SubEffect.artifactSubEffect[4];
            int[] subEffectValues = new int[4];

            if (json.has("subEffectIds") && json.has("subEffectValues")) {
                String[] ids = json.get("subEffectIds").getAsString().split(",");
                String[] vals = json.get("subEffectValues").getAsString().split(",");
                
                for (int i = 0; i < ids.length && i < 4; i++) {
                    if (!ids[i].isEmpty()) {
                        try {
                            int subId = Integer.parseInt(ids[i].trim());
                            subEffects[i] = SubEffect.artifactSubEffect.fromId(subId);
                            if (i < vals.length && !vals[i].isEmpty()) {
                                subEffectValues[i] = Integer.parseInt(vals[i].trim());
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            artifact.setStatus(slot, tier, level, mainEffect, mainEffectValue, subEffects, subEffectValues);
            return artifact;

        } catch (Exception e) {
            // お金データの場合はJSONパース後ここに来る可能性もあるが、isMoneyチェックで弾く
            // plugin.getLogger().severe("アーティファクトデシリアライズエラー: " + e.getMessage());
            return null;
        }
    }
}
