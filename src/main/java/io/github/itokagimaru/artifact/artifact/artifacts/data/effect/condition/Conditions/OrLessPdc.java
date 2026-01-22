package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.DoSetPDC;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class OrLessPdc implements ConditionWithoutEvent{
    Values values;
    NamespacedKey key;
    public OrLessPdc(Values values, String key){
        this.values = values;
        this.key = new NamespacedKey("artifact", key);
    }

    @Override
    public boolean isTrue(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        Object targetValueObj = player.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
        if (targetValueObj == null){
            return false;
        }
        double targetValue = ByteArrayConverter.ByteToDouble((byte[]) targetValueObj);
        return targetValue >= values.calculate(uuid);
    }
}
