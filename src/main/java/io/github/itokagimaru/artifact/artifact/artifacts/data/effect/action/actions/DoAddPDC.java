package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DoAddPDC extends Action{
    Values values;
    NamespacedKey key;

    public DoAddPDC(Values values, String key) {
        this.values = values;
        this.key = new NamespacedKey("artifact", key);
    }

    @Override
    public void run(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        Object beforeValueObj = player.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
        if (beforeValueObj == null){
            DoSetPDC doSetPDC = new DoSetPDC(values, key);
            doSetPDC.run(playerUuid);
            return;
        }
        double beforeValue = ByteArrayConverter.ByteToDouble((byte[]) beforeValueObj);
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, ByteArrayConverter.toByte(beforeValue + values.calculate(playerUuid)));
    }
}
