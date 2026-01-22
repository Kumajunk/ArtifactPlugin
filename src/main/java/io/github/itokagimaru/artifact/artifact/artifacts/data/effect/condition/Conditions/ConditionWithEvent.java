package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import org.bukkit.event.Event;

import java.util.UUID;

public interface ConditionWithEvent extends Condition {
    boolean isTrue(UUID uuid, Event event);
}
