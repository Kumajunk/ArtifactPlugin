package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ArtifactUpdateListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        ArtifactMain.updatePlayerArtifacts(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        ArtifactMain.updatePlayerArtifacts(player);
    }
}
