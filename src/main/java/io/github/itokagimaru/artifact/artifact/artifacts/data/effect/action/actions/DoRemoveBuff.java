package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.EffectSource;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DoRemoveBuff extends Action {
    public enum RemoveType{
        EACH,
        ALL
    }
    EffectSource effectSource;
    RemoveType removeType;


    public DoRemoveBuff(EffectSource effectSource, RemoveType removeType) {
        this.effectSource = effectSource;
        this.removeType = removeType;
    }
    @Override
    public void run(UUID playerUuid){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(playerUuid);
        if (removeType == RemoveType.ALL) {
            playerStatus.getModifierStack().removeBySource(effectSource.getType(), effectSource.getId());
        } else if (removeType == RemoveType.EACH) {
            playerStatus.getModifierStack().removeEachBySource(effectSource.getType(), effectSource.getId());
        }

        Player player = Bukkit.getPlayer(playerUuid);
        player.sendMessage("バフを消したぜ！！");
    }
}
