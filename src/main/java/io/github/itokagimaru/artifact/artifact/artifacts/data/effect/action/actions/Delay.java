package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.ActionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import org.bukkit.Bukkit;

import java.util.UUID;

public class Delay extends Action {
    ActionStack actions;
    Values delayTime;


    public Delay(ActionStack actions, Values delayTime){
        this.actions = actions;
        this.delayTime = delayTime;
    }

    @Override
    public void run(UUID playerUuid) {
        Bukkit.getScheduler().runTaskLater(ArtifactMain.getInstance(), () -> {
            actions.runActions(playerUuid);
        }, (long) delayTime.calculate(playerUuid));
    }
}
