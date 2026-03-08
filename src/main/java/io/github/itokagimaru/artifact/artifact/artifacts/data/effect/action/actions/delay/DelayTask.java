package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.delay;

import org.bukkit.scheduler.BukkitTask;

public class DelayTask {
    BukkitTask task;
    String key;
    public DelayTask(BukkitTask task, String key) {
        this.task = task;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void cancel() {
        task.cancel();
    }
}
