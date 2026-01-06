package io.github.itokagimaru.artifact.stash;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * プレイヤーログイン時にStashの通知を行うリスナー
 */
public class StashLoginListener implements Listener {

    private StashManager stashManager;

    public void setStashManager(StashManager stashManager) {
        this.stashManager = stashManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (stashManager == null) return;

        Player player = event.getPlayer();
        int stashCount = stashManager.getStashCount(player.getUniqueId());

        if (stashCount > 0) {
            // 少し遅延させてから通知（ログインメッセージの後に表示）
            player.getServer().getScheduler().runTaskLater(
                player.getServer().getPluginManager().getPlugin("artifact"),
                () -> {
                    player.sendMessage("");
                    player.sendMessage("§e§l[Stash] §f保管中のアイテムが §a" + stashCount + "個 §fあります");
                    player.sendMessage("§7/stash で取り出すことができます");
                    player.sendMessage("");
                },
                40L  // 2秒後
            );
        }
    }
}
