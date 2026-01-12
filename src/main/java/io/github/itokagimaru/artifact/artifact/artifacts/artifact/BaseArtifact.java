package io.github.itokagimaru.artifact.artifact.artifacts.artifact;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffectTable;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffectTable;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;

import java.util.*;

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

    public void performEnhance(){
        if (this.series.getExStatus() != null) {
            for (ExceptionStatus.artifactExceptionStatus status : this.series.getExStatus()) {
                if (status == ExceptionStatus.artifactExceptionStatus.CANNOT_ENHANCE) {
                    return;
                }
            }
        }

        if (this.lv >= 30) {
            return;
        }

        this.lv++;

        Double baseGrowth = MainEffectTable.mainEffectBaseGrowthRate.get(this.mainEffect);
        if (baseGrowth != null) {
            // MainEffectTable 内の mainEffectExceptionGrowthRate (1.5倍ボーナス等) を適用
            // lv は 1 から始まるため、配列のインデックスは lv - 1 を使用する
            double multiplier = MainEffectTable.mainEffectExceptionGrowthRate[this.lv - 1];

            this.mainEffectValue += baseGrowth * multiplier;
            this.mainEffectValue = Math.round(this.mainEffectValue * 10000.0) / 10000.0;
        }

        Random random = new Random();
        if (this.lv <= 2) {
            int emptyIndex = -1;
            for (int i = 0; i < this.subEffects.length; i++) {
                if (this.subEffects[i] == null) {
                    emptyIndex = i;
                    break;
                }
            }

            if (emptyIndex != -1) {
                List<SubEffect.artifactSubEffect> available = new ArrayList<>();
                for (SubEffect.artifactSubEffect type : SubEffect.artifactSubEffect.values()) {
                    boolean alreadyHas = false;
                    for (SubEffect.artifactSubEffect current : this.subEffects) {
                        if (type == current) {
                            alreadyHas = true;
                            break;
                        }
                    }
                    if (!alreadyHas) available.add(type);
                }

                if (!available.isEmpty()) {
                    SubEffect.artifactSubEffect newEffect = available.get(random.nextInt(available.size()));
                    List<Double> values = SubEffectTable.subEffectValueTable.get(newEffect);
                    if (values != null && !values.isEmpty()) {
                        this.subEffects[emptyIndex] = newEffect;
                        double newValue = values.get(random.nextInt(values.size()));
                        this.subEffectsValue[emptyIndex] = Math.round(newValue * 10000.0) / 10000.0;
                    }
                }
            }
        } else {
            List<Integer> existingIndices = new ArrayList<>();
            for (int i = 0; i < this.subEffects.length; i++) {
                if (this.subEffects[i] != null) existingIndices.add(i);
            }

            if (!existingIndices.isEmpty()) {
                int targetIndex = existingIndices.get(random.nextInt(existingIndices.size()));
                SubEffect.artifactSubEffect effect = this.subEffects[targetIndex];
                List<Double> values = SubEffectTable.subEffectValueTable.get(effect);
                if (values != null && !values.isEmpty()) {
                    this.subEffectsValue[targetIndex] += values.get(random.nextInt(values.size()));
                    this.subEffectsValue[targetIndex] = Math.round(this.subEffectsValue[targetIndex] * 10000.0) / 10000.0;
                }
            }
        }
    }
}
