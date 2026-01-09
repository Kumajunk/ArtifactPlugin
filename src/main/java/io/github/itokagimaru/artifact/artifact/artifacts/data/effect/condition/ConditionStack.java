package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.Condition;

import java.util.UUID;

public class ConditionStack {
    Condition[] conditions;
    public ConditionStack(Condition[] conditions){
        this.conditions = conditions;
    }

    public boolean isAllTrue(UUID playerUuid){
        boolean flag = true;
        for (Condition condition : conditions){
            if (!condition.isTrue(playerUuid)){
                flag = false;
                break;
            }
        }
        return flag;
    }
}
