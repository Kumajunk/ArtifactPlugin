package io.github.itokagimaru.artifact.Artifact.Artifacts.ArtifactFactory;

import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.ExceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.MainEffect.MainEffect;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.MainEffect.MainEffectTable;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Series.Series;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Slot.Slot;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.SubEffect.SubEffect;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.SubEffect.SubEffectTable;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Tire.Tier;
import io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Tire.TierTable;
import io.github.itokagimaru.artifact.Artifact.Artifacts.series.Base.BaseArtifact;
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

    public int getMainEffectValue(MainEffect.artifactMainEffect mainEffect){
        return MainEffectTable.mainEffectInitialValue.get(mainEffect);
    }

    public SubEffect.artifactSubEffect[] getRandSubEffects(Series.artifactSeres seres){

        SubEffect.artifactSubEffect[] subEffects = new SubEffect.artifactSubEffect[2];
        if (!ExceptionStatus.isHaveExceptionStatus(seres, ExceptionStatus.artifactExceptionStatus.CANNOT_HAVE_SUB_EFFECT)){
            Random rand = new Random();
            for (int i = 0; i < subEffects.length; i++){
                int r = rand.nextInt(SubEffect.artifactSubEffect.effectSize);
                subEffects[i] = SubEffect.artifactSubEffect.fromId(r);
            }
        }
        return subEffects;
    }

    public int[] getSubEffectsValue(SubEffect.artifactSubEffect[] subEffects){
        int[] subEffectsValue = new int[subEffects.length];
        Random rand = new Random();
        SubEffect.artifactSubEffect subEffect;
        for (int i = 0; i < subEffectsValue.length; i++){
            subEffect = subEffects[i];
            if (subEffect == null) continue;
            List<Integer> table = SubEffectTable.subEffectValueTable.get(subEffect);
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
        int mainEffectValue = getMainEffectValue(mainEffect);
        SubEffect.artifactSubEffect[] subEffects = getRandSubEffects(seres);
        int[] subEffectsValue = getSubEffectsValue(subEffects);
        BaseArtifact artifact = seres.getArtifactType();
        artifact.setStatus(slot, tier, 0, mainEffect, mainEffectValue, subEffects, subEffectsValue);
        return artifact;
    }
    public ItemStack makeNewArtifact(Series.artifactSeres seres, Slot.artifactSlot slot, Tier.artifactTier tier){
        return ArtifactToItem.convert(makeNewArtifactData(seres, slot, tier));
    }
}
