package io.github.itokagimaru.artifact.artifact.artifacts.artifact;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tire.Tier;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BaseArtifact {
    protected UUID artifactId;
    protected Series series;
    protected Tier.artifactTier tire;
    protected int lv;
    protected MainEffect.artifactMainEffect mainEffect;
    protected double mainEffectValue;//mainEffectVal*10^-1=text%
    protected SubEffect.artifactSubEffect[] subEffects;
    protected double[] subEffectsValue;
    protected Slot.artifactSlot slot;

    public void setStatus(Series series, Slot.artifactSlot slot, Tier.artifactTier tire, int lv, MainEffect.artifactMainEffect mainEffect, double mainEffectValue, SubEffect.artifactSubEffect[] subEffects, double[] subEffectsValue){
        artifactId = UUID.randomUUID();
        this.series = series;
        this.slot = slot;
        this.tire = tire;
        this.lv = lv;
        this.mainEffect = mainEffect;
        this.mainEffectValue = mainEffectValue;
        if (subEffects.length != 4) subEffects = Arrays.copyOf(subEffects,4);
        this.subEffects = subEffects;
        if (subEffectsValue.length != 4) subEffectsValue = Arrays.copyOf(subEffectsValue,4);
        this.subEffectsValue = subEffectsValue;
    }
    public UUID getUUID() {
        return artifactId;
    }
    public Tier.artifactTier getTier(){
        return tire;
    }
    public Slot.artifactSlot getSlot() {
        return slot;
    }
    public Series getSeries(){
        return series;
    }
    public int getLv(){
        return lv;
    }
    public MainEffect.artifactMainEffect getMainEffect(){
        return mainEffect;
    }
    public double getMainEffectValue(){
        return mainEffectValue;
    }
    public SubEffect.artifactSubEffect[] getSubEffects(){
        return subEffects;
    }
    public double[] getSubEffectsValue(){
        return subEffectsValue;
    }
}
