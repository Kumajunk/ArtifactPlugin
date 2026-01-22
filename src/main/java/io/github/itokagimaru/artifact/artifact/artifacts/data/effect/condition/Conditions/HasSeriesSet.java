package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffectUpdater;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffectUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.UUID;

import static io.github.itokagimaru.artifact.artifact.EquipPdc.loadFromPdc;

public class HasSeriesSet implements ConditionWithoutEvent {
    String seriesName;
    int requiredCount;

    public HasSeriesSet(String seriesName, int requiredCount){
        this.seriesName = seriesName;
        this.requiredCount = requiredCount;
    }

    public boolean isTrue(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        int setCount = 0;
        if (player == null) return false;
        for (Slot.artifactSlot slot : Slot.artifactSlot.values()) {
            BaseArtifact artifact = loadFromPdc(player, slot);
            if (artifact == null) continue;
            if (artifact.getSeries().getInternalName().equals(seriesName))
                setCount++;
        }
        return setCount >= requiredCount;
    }
}
