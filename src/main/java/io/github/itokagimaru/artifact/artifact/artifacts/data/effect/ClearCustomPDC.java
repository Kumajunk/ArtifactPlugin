package io.github.itokagimaru.artifact.artifact.artifacts.data.effect;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.UUID;

public class ClearCustomPDC {
    public static void clear(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        PersistentDataContainer persistentDataContainer = player.getPersistentDataContainer();
        for (NamespacedKey key : persistentDataContainer.getKeys()) {
            if (!key.getNamespace().equals("artifacts_custom")) persistentDataContainer.remove(key);
        }
    }
}
