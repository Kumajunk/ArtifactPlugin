package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;

import java.util.UUID;

public class ActionStack {
    Action[] actions;

    public ActionStack(Action[] actions){
        this.actions = actions;
    }



    public void runActions(UUID playerUuid){
        for (Action action : actions){
            action.run(playerUuid);
        }
    }
}
