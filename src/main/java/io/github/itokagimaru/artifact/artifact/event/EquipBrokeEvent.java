package io.github.itokagimaru.artifact.artifact.event;

import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

/**
 * アーティファクトの耐久値が0になり破損した際に発火するイベント
 */
public class EquipBrokeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BaseArtifact artifact;
    private final Slot.artifactSlot slot;

    public EquipBrokeEvent(Player player, BaseArtifact artifact, Slot.artifactSlot slot) {
        this.player = player;
        this.artifact = artifact;
        this.slot = slot;
    }

    public Player getPlayer() {
        return player;
    }

    public BaseArtifact getArtifact() {
        return artifact;
    }

    public Slot.artifactSlot getSlot() {
        return slot;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
