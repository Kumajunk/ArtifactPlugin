package io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect;

import java.util.HashMap;

public class SubEffect {
    public static enum artifactSubEffect {//MainにはLUKがあるが、Subにはないよ
        HP(0,"HP"),
        ATK(1,"攻撃力"),
        DEF(2,"防御力"),
        VIT(3,"回復補正"),
        CRI(4,"会心率"),
        CRIDMG(5,"会心ダメージ"),
        FIRE_DMG_REDUCE(6,"属性軽減 -火-"),
        WATER_DMG_REDUCE(7,"属性軽減 -水-"),
        NATURE_DMG_REDUCE(8,"属性軽減 -木-");
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
