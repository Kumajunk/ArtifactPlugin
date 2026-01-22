package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import java.util.UUID;

public interface ConditionWithoutEvent extends Condition {
    boolean isTrue(UUID uuid);
}
