package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.EffectSource;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;

import java.util.UUID;

public class DoRemoveBuff extends Action {
    EffectSource effectSource;
    public DoRemoveBuff(EffectSource effectSource){
        this.effectSource = effectSource;
    }
    @Override
    public void run(UUID playerUuid){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(playerUuid);
        playerStatus.getModifierStack().removeBySource(effectSource.getType(), effectSource.getId());
    }
}
