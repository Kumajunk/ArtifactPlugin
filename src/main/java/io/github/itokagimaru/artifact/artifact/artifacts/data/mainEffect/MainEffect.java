package io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect;


import java.util.HashMap;

public class MainEffect {
    public static enum artifactMainEffect {
        HP(0,"HP", valueType.MULTIPLY),
        ATK(1,"攻撃力", valueType.MULTIPLY),
        DEF(2,"防御力", valueType.MULTIPLY),
        AGI(3,"移動速度", valueType.MULTIPLY),
        VIT(4,"回復補正", valueType.MULTIPLY),
        CRI(5,"会心率", valueType.MULTIPLY),
        CRIDMG(6,"会心ダメージ", valueType.MULTIPLY),
        FIRE_DMG_BONUS(7,"属性ダメージ -火-", valueType.MULTIPLY),
        WATER_DMG_BONUS(8,"属性ダメージ -水-",valueType.MULTIPLY),
        NATURE_DMG_BONUS(9,"属性ダメージ -木-", valueType.MULTIPLY);
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
