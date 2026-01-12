package io.github.itokagimaru.artifact.artifact.artifacts.data.effect;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.ActionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.ConditionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.Condition;

import java.util.UUID;

public class Effect {
    TriggerType.triggerType triggerType;
    ActionStack actions;
    ConditionStack conditions;

    public Effect(TriggerType.triggerType triggerType, Condition[] conditions, Action[] actions){
        this.triggerType = triggerType;
        this.conditions = new ConditionStack(conditions);
        this.actions = new ActionStack(actions);
    }
    public void run(UUID playerUUID){
        if (!conditions.isAllTrue(playerUUID)) return;
        actions.runActions(playerUUID);
    }

    public TriggerType.triggerType getTriggerType(){
        return triggerType;
    }
}
