package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.Condition;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.ConditionWithEvent;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.ConditionWithoutEvent;
import org.bukkit.event.Event;

import java.util.UUID;

public class ConditionStack {
    Condition[] conditions;
    public ConditionStack(Condition[] conditions){
        this.conditions = conditions;
    }

    public boolean isAllTrue(UUID playerUuid, Event event){
        boolean flag = true;
        for (Condition condition : conditions){
            if (condition instanceof ConditionWithoutEvent conditionWoE){
                if (!conditionWoE.isTrue(playerUuid)){
                    flag = false;
                    break;
                }
            }else if (condition instanceof ConditionWithEvent conditionWE){
                if (event == null) {
                    flag = false;
                    break;
                }
                if (!conditionWE.isTrue(playerUuid, event)){
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }
}
