package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.delay;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.ActionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.ConditionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class Delay extends Action {
    ActionStack actions;
    Values delayTime;
    ConditionStack delayCondition;


    public Delay(ActionStack actions, Values delayTime, ConditionStack delayCondition) {
        this.actions = actions;
        this.delayTime = delayTime;
        this.delayCondition = delayCondition;
    }

    @Override
    public void run(UUID playerUuid) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(ArtifactMain.getInstance(), () -> {
            if (Bukkit.getPlayer(playerUuid) == null) return;
            if (!delayCondition.isAllTrue(playerUuid, null)) return;
            actions.runActions(playerUuid);
        }, (long) delayTime.calculate(playerUuid));
        TaskStack.setTasks(playerUuid, task);
    }
}
