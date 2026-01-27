package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OrLessHp implements ConditionWithoutEvent {
    Values values;
    boolean isMultiply;

    public OrLessHp(Values values, boolean isMultiply){
        this.values = values;
        this.isMultiply = isMultiply;
    }

    @Override
    public boolean isTrue(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (isMultiply){
            PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(uuid);
            double maxHp = playerStatus.getStatus(PlayerStatus.playerStatus.HP);
            double nowHp = player.getHealth();
            return values.calculate(uuid) >= nowHp / maxHp;
        }else{
            double nowHp = player.getHealth();
            return values.calculate(uuid) >= nowHp;
        }
    }

}
