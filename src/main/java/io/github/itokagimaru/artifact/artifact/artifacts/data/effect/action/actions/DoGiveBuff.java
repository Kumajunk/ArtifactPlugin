package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.EffectSource;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.Player.status.StatusModifier;

import java.util.UUID;

public class DoGiveBuff extends Action {
    PlayerStatus.playerStatus status;
    double value;
    StatusModifier.ValueType valueType;
    String key;
    EffectSource.EffectSourceType effectSourceType;

    DoGiveBuff(PlayerStatus.playerStatus status, double value, StatusModifier.ValueType valueType, String key, EffectSource.EffectSourceType effectSourceType){
        this.status = status;
        this.value = value;
        this.valueType = valueType;
        this.key = key;
        this.effectSourceType = effectSourceType;
    }

    @Override
    public void run(UUID playerUuid){
        EffectSource source = new EffectSource(effectSourceType, key);
        StatusModifier modifier = new StatusModifier(UUID.randomUUID(), status, valueType, value, source);

        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(playerUuid);
        playerStatus.getModifierStack().add(modifier);
        PlayerStatusManager.addPlayerStatus(playerUuid, playerStatus);
    }
}
