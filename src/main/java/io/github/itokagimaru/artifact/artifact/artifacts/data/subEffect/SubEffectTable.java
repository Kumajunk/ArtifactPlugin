package io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect;

import java.util.HashMap;
import java.util.List;

public class SubEffectTable {
    public static HashMap<SubEffect.artifactSubEffect, List<Double>> subEffectValueTable = new HashMap<>();
    static {
        subEffectValueTable.put(null,null);
        subEffectValueTable.put(SubEffect.artifactSubEffect.HP,List.of(0.004,0.005,0.007));
        subEffectValueTable.put(SubEffect.artifactSubEffect.ATK,List.of(0.005,0.006,0.008));
        subEffectValueTable.put(SubEffect.artifactSubEffect.DEF,List.of(0.004,0.005,0.007));
        subEffectValueTable.put(SubEffect.artifactSubEffect.VIT,List.of(0.002,0.003,0.005));
        subEffectValueTable.put(SubEffect.artifactSubEffect.CRI,List.of(0.001,0.002,0.004));
        subEffectValueTable.put(SubEffect.artifactSubEffect.CRIDMG,List.of(0.005,0.006,0.008));
        subEffectValueTable.put(SubEffect.artifactSubEffect.FIRE_DMG_REDUCE,List.of(0.001,0.002,0.003));
        subEffectValueTable.put(SubEffect.artifactSubEffect.WATER_DMG_REDUCE,List.of(0.001,0.002,0.003));
        subEffectValueTable.put(SubEffect.artifactSubEffect.NATURE_DMG_REDUCE,List.of(0.001,0.002,0.003));
    }
}
