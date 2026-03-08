package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.delay;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;

import java.util.UUID;

public class DelayCancel extends Action {
    private String key;
    public DelayCancel(String key){
        this.key = key;
    }
    @Override
    public void run(UUID playerUuid) {
        TaskStack.cancelTasks(playerUuid, key);
    }
}
