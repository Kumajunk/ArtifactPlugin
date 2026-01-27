package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DoEmitParticle extends Action {
    Particle particle;
    double offsetX, offsetY, offsetZ;
    int count;
    public DoEmitParticle(String particleStr, double offsetX, double offsetY, double offsetZ, int count) {
        particle = Particle.valueOf(particleStr.toUpperCase());
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.count = count;
    }

    @Override
    public void run(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        player.getWorld().spawnParticle(particle, player.getLocation().clone().add(offsetX, offsetY, offsetZ), count);
        player.sendMessage(particle.toString() + " has been emitted.");
    }
}
