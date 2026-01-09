package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import java.util.UUID;

public abstract class Condition {
    public abstract boolean isTrue(UUID playerUuid);
}
