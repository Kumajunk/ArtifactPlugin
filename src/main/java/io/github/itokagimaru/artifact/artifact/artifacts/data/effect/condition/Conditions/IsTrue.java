package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import org.bukkit.event.Event;

import java.util.UUID;

public class IsTrue implements ConditionWithoutEvent {
    public boolean isTrue(UUID uuid) {
        return true;
    }
}
