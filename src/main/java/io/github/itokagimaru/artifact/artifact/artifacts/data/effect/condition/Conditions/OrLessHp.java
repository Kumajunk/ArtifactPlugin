package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import org.bukkit.Bukkit;

import java.util.UUID;

public class OrLessHp extends Condition {
    Values values;
    boolean isMultiply;

    public OrLessHp(Values values, boolean isMultiply){
        this.values = values;
        this.isMultiply = isMultiply;
    }

    @Override
    public boolean isTrue(UUID uuid) {
        if (isMultiply){
            PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(uuid);
            double maxHp = playerStatus.getStatus(PlayerStatus.playerStatus.HP);
            double nowHp = Bukkit.getEntity(uuid).getHeight();
            return values.calculate(uuid) > nowHp / maxHp;
        }else{

            double nowHp = Bukkit.getEntity(uuid).getHeight();
            return values.calculate(uuid) >= nowHp;
        }
    }

}
