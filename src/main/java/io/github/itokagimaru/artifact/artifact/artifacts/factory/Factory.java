package io.github.itokagimaru.artifact.artifact.artifacts.factory;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffectTable;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffectTable;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.TierTable;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class Factory {
    public Slot.artifactSlot getRandSlot(){
        Random rand = new Random();
        int r = rand.nextInt(Slot.artifactSlot.slotSize);
        return Slot.artifactSlot.fromId(r);
    }

    public MainEffect.artifactMainEffect getRandMainEffect(Series.artifactSeres seres, Slot.artifactSlot slot){
        if (ExceptionStatus.isHaveExceptionStatus(seres, ExceptionStatus.artifactExceptionStatus.MAIN_EFFECT_FIXED)){
            return MainEffectTable.ExceptionMainEffectTable.get(slot).getFirst();
        }
        List<MainEffect.artifactMainEffect> table = MainEffectTable.mainEffectTable.get(slot);
        Random rand = new Random();
        int r = rand.nextInt(table.size());
        return table.get(r);
    }

    public double getMainEffectValue(MainEffect.artifactMainEffect mainEffect){
        return MainEffectTable.mainEffectInitialValue.get(mainEffect);
    }

    public SubEffect.artifactSubEffect[] getRandSubEffects(Series.artifactSeres seres){

        SubEffect.artifactSubEffect[] subEffects = new SubEffect.artifactSubEffect[2];
        SubEffect.artifactSubEffect subEffectTemp = null;
        boolean flag;
        if (!ExceptionStatus.isHaveExceptionStatus(seres, ExceptionStatus.artifactExceptionStatus.CANNOT_HAVE_SUB_EFFECT)){
            Random rand = new Random();
            for (int i = 0; i < subEffects.length; i++){
                flag = true;
                while (flag){
                    flag = false;
                    int r = rand.nextInt(SubEffect.artifactSubEffect.effectSize);
                    subEffectTemp = SubEffect.artifactSubEffect.fromId(r);
                    for(SubEffect.artifactSubEffect effect : subEffects){
                        if (effect == subEffectTemp) flag = true;
                    }
                }
                subEffects[i] = subEffectTemp;
            }
        }
        return subEffects;
    }

    public double[] getSubEffectsValue(SubEffect.artifactSubEffect[] subEffects){
        double[] subEffectsValue = new double[subEffects.length];
        Random rand = new Random();
        SubEffect.artifactSubEffect subEffect;
        for (int i = 0; i < subEffectsValue.length; i++){
            subEffect = subEffects[i];
            if (subEffect == null) continue;
            List<Double> table = SubEffectTable.subEffectValueTable.get(subEffect);
            int r = rand.nextInt(table.size());
            subEffectsValue[i] = table.get(r);
        }
        return subEffectsValue;
    }

    public Tier.artifactTier getRandTier(TierTable.dropRateLv lv){
        Tier.artifactTier[] table = lv.getDropTier;
        Random rand = new Random();
        double r = rand.nextDouble();
        r -= TierTable.dropRate.HIGH.getRate;
        if (r < 0) {
            return table[0];
        }
        r -= TierTable.dropRate.MIDDLE.getRate;
        if (r < 0) {
            return table[1];
        }
        return table[2];
    }

    public BaseArtifact makeNewArtifactData(Series.artifactSeres seres, Slot.artifactSlot slot, Tier.artifactTier tier){
        MainEffect.artifactMainEffect mainEffect = getRandMainEffect(seres, slot);
        double mainEffectValue = getMainEffectValue(mainEffect);
        SubEffect.artifactSubEffect[] subEffects = getRandSubEffects(seres);
        double[] subEffectsValue = getSubEffectsValue(subEffects);
        BaseArtifact artifact = seres.getArtifactType();
        artifact.setStatus(slot, tier, 0, mainEffect, mainEffectValue, subEffects, subEffectsValue);
        return artifact;
    }
    public ItemStack makeNewArtifact(Series.artifactSeres seres, Slot.artifactSlot slot, Tier.artifactTier tier){
        return ArtifactToItem.convert(makeNewArtifactData(seres, slot, tier));
    }
}
