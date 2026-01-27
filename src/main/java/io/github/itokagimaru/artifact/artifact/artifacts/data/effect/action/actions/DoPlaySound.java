package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DoPlaySound extends Action {
    Sound sound;
    float volume;
    float pitch;
    public DoPlaySound(String sound, float volume, float pitch) {
        stringToSound(normalizeSoundKey(sound));
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void run(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    private void stringToSound(String sound) {
        NamespacedKey key = NamespacedKey.minecraft(sound);
        this.sound = Bukkit.getRegistry(Sound.class).get(key);
    }

    private String normalizeSoundKey(String str) {
        return str.toLowerCase().replace("_", ".").replace("-", ".");
    }

}
