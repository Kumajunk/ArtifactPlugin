package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import org.bukkit.Bukkit;

import java.util.UUID;

public class BlowHp extends Condition {
    double threshold;

    BlowHp(double threshold){
        this.threshold = threshold;
    }

    @Override
    public boolean isTrue(UUID uuid){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(uuid);
        double maxHp = playerStatus.getStatus(PlayerStatus.playerStatus.HP);
        double nowHp = Bukkit.getEntity(uuid).getHeight();
        return threshold > nowHp / maxHp;
    }

}
