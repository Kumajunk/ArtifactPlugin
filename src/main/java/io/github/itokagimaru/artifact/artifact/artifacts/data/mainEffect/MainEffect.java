package io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect;


import java.util.HashMap;

public class MainEffect {
    public static enum artifactMainEffect {
        HP(0,"HitPoint", valueType.MULTIPLY),
        ATK(1,"Attack", valueType.MULTIPLY),
        DEF(2,"Defense", valueType.MULTIPLY),
        LUK(3,"Luck", valueType.ADD),
        VIT(4,"Vitality", valueType.MULTIPLY),
        CRI(5,"CriticalRate", valueType.MULTIPLY),
        CRIDMG(6,"CriticalDamage", valueType.MULTIPLY),
        FIRE_DMG_BONUS(7,"FireDamageBonus", valueType.MULTIPLY),
        WATER_DMG_BONUS(8,"WaterDamageBonus",valueType.MULTIPLY),
        NATURE_DMG_BONUS(9,"NatureDamageBonus", valueType.MULTIPLY);
        public final String getText;
        public final int getId;
        public final String getAddTypeText;
        public final valueType getAddType;
        artifactMainEffect(int id, String textName, valueType valType){
            this.getText = textName;
            this.getId = id;
            this.getAddTypeText = valType.getAddTypeText;
            this.getAddType = valType;
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
    public enum valueType{
        ADD(""),
        MULTIPLY("%");
        public final String getAddTypeText;
        valueType(String addTypeText){
            this.getAddTypeText = addTypeText;
        }
    }
}
