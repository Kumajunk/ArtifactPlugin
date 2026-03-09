package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.Player.status.StatusModifier;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import org.bukkit.Bukkit;

import java.util.UUID;

public class DoHeal extends Action{
    Values value;
    boolean isMultiply;
    public DoHeal(Values value, boolean isMultiply) {
        this.value = value;
        this.isMultiply = isMultiply;
    }

    @Override
    public void run(UUID playerUuid){
        double playerHp = Bukkit.getPlayer(playerUuid).getHealth();
        double playerMaxHp = Bukkit.getPlayer(playerUuid).getMaxHealth();
        if(isMultiply){
            playerHp = playerMaxHp * value.calculate(playerUuid);
        } else {
            double calculatedValue = value.calculate(playerUuid);
            PlayerStatus status = PlayerStatusManager.getPlayerStatus(playerUuid);
            double vit = status.getStatus(PlayerStatus.playerStatus.VIT);
            if (calculatedValue < 0) playerHp = playerHp + calculatedValue;
            else playerHp = playerHp + (calculatedValue * (1 + vit));
        }
        if(playerHp < 0) playerHp = 0;
        if (playerHp > playerMaxHp) playerHp = playerMaxHp;
        Bukkit.getPlayer(playerUuid).setHealth(playerHp);
        EffectStack.runByTrigger(TriggerType.triggerType.ON_HEAL, playerUuid);
    }
}
