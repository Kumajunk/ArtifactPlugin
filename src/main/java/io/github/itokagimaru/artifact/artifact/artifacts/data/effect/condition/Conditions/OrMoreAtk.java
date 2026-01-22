package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;

import java.util.UUID;

public class OrMoreAtk implements ConditionWithoutEvent{
    Values values;
    boolean isMultiply;

    public OrMoreAtk(Values values, boolean isMultiply){
        this.values = values;
        this.isMultiply = isMultiply;
    }

    @Override
    public boolean isTrue(UUID uuid) {
        if (!isMultiply) {
            PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(uuid);
            double atk = playerStatus.getStatus(PlayerStatus.playerStatus.ATK);
            return values.calculate(uuid) <= atk;
        }
        return false;
    }
}
