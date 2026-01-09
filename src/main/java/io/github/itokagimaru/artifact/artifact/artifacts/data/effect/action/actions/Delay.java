package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.ArtifactMain;
import org.bukkit.Bukkit;

import java.util.UUID;

public class Delay extends Action {
    Action action;
    int delayTime;


    Delay(Action action, int delayTime){
        this.action = action;
        this.delayTime = delayTime;
    }

    @Override
    public void run(UUID playerUuid){
        Bukkit.getScheduler().runTaskLater(ArtifactMain.getInstance(), () -> {
            action.run(playerUuid);
        }, delayTime);
    }
}
