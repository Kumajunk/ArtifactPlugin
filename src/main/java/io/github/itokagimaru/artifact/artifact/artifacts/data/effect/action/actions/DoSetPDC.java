package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import io.github.itokagimaru.artifact.data.EntityData;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DoSetPDC extends Action{
    Values values;
    NamespacedKey key;

    public DoSetPDC(Values values, String key) {
        this.values = values;
        this.key = new NamespacedKey("artifact", key);
    }
    public DoSetPDC(Values values, NamespacedKey key) {
        this.values = values;
        this.key = key;
    }

    @Override
    public void run(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, ByteArrayConverter.toByte(values.calculate(playerUuid)));
    }
}
