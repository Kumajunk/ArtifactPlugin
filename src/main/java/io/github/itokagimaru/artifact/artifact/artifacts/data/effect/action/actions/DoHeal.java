package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.StatusModifier;
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
            playerHp = playerHp * value.calculate(playerUuid);
        } else {
            playerHp = playerHp + value.calculate(playerUuid);
        }
        if (playerHp > playerMaxHp) playerHp = playerMaxHp;
        Bukkit.getPlayer(playerUuid).setHealth(playerHp);
    }
}
