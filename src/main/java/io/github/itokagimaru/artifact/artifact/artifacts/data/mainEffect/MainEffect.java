package io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect;


import java.util.HashMap;

public class MainEffect {
    public static enum artifactMainEffect {
        HP(0,"HitPoint"),
        ATK(1,"Attack"),
        DEF(2,"Defense"),
        LUK(3,"Luck"),
        VIT(4,"Vitality"),
        CRI(5,"CriticalRate"),
        CRIDMG(6,"CriticalDamage"),
        FIRE_DMG_BONUS(7,"FireDamageBonus"),
        WATER_DMG_BONUS(8,"WaterDamageBonus"),
        NATURE_DMG_BONUS(9,"NatureDamageBonus");
        public final String getText;
        public final int getId;
        artifactMainEffect(int id, String textName){
            this.getText = textName;
            this.getId = id;
        }
        public static final HashMap<Integer, artifactMainEffect> mainEffectHashMap = new HashMap<>();
        public static int effectSize = 0;
        static {
            for (artifactMainEffect effect : values()){
                mainEffectHashMap.put(effect.getId,effect);
                effectSize++;
            }
        }
        public static artifactMainEffect fromId(int value){
            return mainEffectHashMap.get(value);
        }
    }
}
