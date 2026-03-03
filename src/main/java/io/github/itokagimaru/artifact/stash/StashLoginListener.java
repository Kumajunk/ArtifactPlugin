package io.github.itokagimaru.artifact.stash;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

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
        // Stash内のアイテム数を非同期で取得し、メインスレッドで通知します。
        stashManager.getStashCount(player.getUniqueId()).thenAccept(stashCount -> {
            if (stashCount <= 0) return;
            // 参加メッセージの後に通知を表示するため、2秒間遅延させます（2秒後）。
            player.getServer().getScheduler().runTaskLater(
                    Objects.requireNonNull(player.getServer().getPluginManager().getPlugin("artifact")),
                () -> {
                    player.sendMessage("");
                    player.sendMessage("§e§l[Stash] §f保管中のアイテムが §a" + stashCount + "個 §fあります");
                    player.sendMessage("§7/stash で取り出すことができます");
                    player.sendMessage("");
                },
                40L  // 2 seconds
            );
        });
    }
}
