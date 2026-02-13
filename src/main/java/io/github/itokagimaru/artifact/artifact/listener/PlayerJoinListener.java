package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        ArtifactMain.updatePlayerArtifacts(e.getPlayer());
    }
}
