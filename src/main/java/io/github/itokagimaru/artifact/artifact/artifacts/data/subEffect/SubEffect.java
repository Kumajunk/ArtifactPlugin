package io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect;

import java.util.HashMap;

public class SubEffect {
    public static enum artifactSubEffect {//MainにはLUKがあるが、Subにはないよ
        HP(0,"HitPoint"),
        ATK(1,"Attack"),
        DEF(2,"Defense"),
        VIT(3,"Vitality"),
        CRI(4,"CriticalRate"),
        CRIDMG(5,"CriticalDamage"),
        FIRE_DMG_REDUCE(6,"FireDamageReduceRate"),
        WATER_DMG_REDUCE(7,"WaterDamageReduceRate"),
        NATURE_DMG_REDUCE(8,"NatureDamageReduceRate");
        public final String getText;
        public int getId = -1;
        artifactSubEffect(int id, String textName){
            this.getText = textName;
            this.getId = id;
        }
        public static final HashMap<Integer, artifactSubEffect> subEffectHashMap = new HashMap<>();
        public static int effectSize = 0;
        static {
            for (artifactSubEffect effect : values()){
                subEffectHashMap.put(effect.getId,effect);
                effectSize++;
            }
        }
        public static artifactSubEffect fromId(int value){
            return subEffectHashMap.get(value);
        }
    }
}
