package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;


public class PlayerReSpawnListener implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        ArtifactMain.updatePlayerArtifacts(player);

        //ステータスを見える化アイテムが存在しなければつけます
        if(player.getInventory().getItem(39) != null) return;
        player.getInventory().setItem(39, SpecialItems.getStatViewer(player));
    }
}
