package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.delay;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TaskStack {
    private static HashMap<UUID, List<BukkitTask>> tasksMap = new HashMap<UUID, List<BukkitTask>>();

    public static List<BukkitTask> getTasks(UUID playerUUID) {
        return tasksMap.get(playerUUID);
    }

    public static void setTasks(UUID playerUUID, List<BukkitTask> tasks) {
        tasksMap.put(playerUUID, tasks);
    }

    public static void setTasks(UUID playerUUID, BukkitTask task) {
        List<BukkitTask> tasks = tasksMap.get(playerUUID);
        if (tasks == null) tasks = new ArrayList<BukkitTask>();
        tasks.add(task);
        setTasks(playerUUID, tasks);
    }

    public static void cancelTasks(UUID playerUUID){
        List<BukkitTask> tasks = tasksMap.get(playerUUID);
        if(tasks==null || tasks.isEmpty()) return;
        for(BukkitTask task : tasks){
            task.cancel();
        }
    }
}
