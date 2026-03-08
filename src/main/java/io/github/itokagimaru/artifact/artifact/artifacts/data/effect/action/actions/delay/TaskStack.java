package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.delay;

import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TaskStack {
    private static final HashMap<UUID, List<DelayTask>> tasksMap = new HashMap<UUID, List<DelayTask>>();

    public static List<DelayTask> getTasks(UUID playerUUID) {
        return tasksMap.get(playerUUID);
    }

    public static void setTasks(UUID playerUUID, List<DelayTask> tasks) {
        tasksMap.put(playerUUID, tasks);
    }

    public static void setTasks(UUID playerUUID, DelayTask task) {
        List<DelayTask> tasks = tasksMap.get(playerUUID);
        if (tasks == null) tasks = new ArrayList<DelayTask>();
        tasks.add(task);
        setTasks(playerUUID, tasks);
    }

    public static void cancelAllTasks(UUID playerUUID){
        List<DelayTask> tasks = tasksMap.get(playerUUID);
        if(tasks==null || tasks.isEmpty()) return;
        for(DelayTask task : tasks){
            task.cancel();
        }
        tasksMap.remove(playerUUID);
    }

    public static void cancelTasks(UUID playerUUID, String key){
        List<DelayTask> tasks = tasksMap.get(playerUUID);
        if(tasks == null || tasks.isEmpty()) return;

        Iterator<DelayTask> it = tasks.iterator();

        while(it.hasNext()){
            DelayTask task = it.next();
            if(key.equals(task.getKey())){
                task.cancel();
                it.remove();
            }
        }

        if(tasks.isEmpty()){
            tasksMap.remove(playerUUID);
        }
    }
}
