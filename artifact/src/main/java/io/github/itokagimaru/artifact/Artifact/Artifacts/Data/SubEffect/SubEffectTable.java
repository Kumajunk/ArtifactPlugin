package io.github.itokagimaru.artifact.Artifact.Artifacts.Data.SubEffect;

import java.util.HashMap;
import java.util.List;

public class SubEffectTable {
    public static HashMap<SubEffect.artifactSubEffect, List<Integer>> subEffectValueTable = new HashMap<>();
    static {
        subEffectValueTable.put(null,null);
        subEffectValueTable.put(SubEffect.artifactSubEffect.HP,List.of(4,5,7));
        subEffectValueTable.put(SubEffect.artifactSubEffect.ATK,List.of(5,6,8));
        subEffectValueTable.put(SubEffect.artifactSubEffect.DEF,List.of(4,5,7));
        subEffectValueTable.put(SubEffect.artifactSubEffect.VIT,List.of(2,3,5));
        subEffectValueTable.put(SubEffect.artifactSubEffect.CRI,List.of(1,2,4));
        subEffectValueTable.put(SubEffect.artifactSubEffect.CRIDMG,List.of(5,6,8));
        subEffectValueTable.put(SubEffect.artifactSubEffect.FIRE_DMG_REDUCE,List.of(1,2,3));
        subEffectValueTable.put(SubEffect.artifactSubEffect.WATER_DMG_REDUCE,List.of(1,2,3));
        subEffectValueTable.put(SubEffect.artifactSubEffect.NATURE_DMG_REDUCE,List.of(1,2,3));
    }
}
