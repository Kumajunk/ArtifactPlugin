package io.github.itokagimaru.artifact.artifact.artifacts.data.effect;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.ActionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.ConditionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.Condition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

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
        Player  player = Bukkit.getPlayer(playerUUID);
        if (!conditions.isAllTrue(playerUUID, null)) return;
        actions.runActions(playerUUID);
    }

    public void run(UUID playerUUID, Event event){
        Player  player = Bukkit.getPlayer(playerUUID);
        if (!conditions.isAllTrue(playerUUID, event)) return;
        actions.runActions(playerUUID);
    }

    public TriggerType.triggerType getTriggerType(){
        return triggerType;
    }
}
