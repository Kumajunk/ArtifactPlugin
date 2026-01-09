package io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect;

import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainEffectTable {
    public static Map<Slot.artifactSlot, List<MainEffect.artifactMainEffect>> mainEffectTable = new HashMap<>();
    static {//MainEffectの抽選テーブル
        mainEffectTable.put(Slot.artifactSlot.PEAR, List.of(MainEffect.artifactMainEffect.HP));
        mainEffectTable.put(Slot.artifactSlot.OVAL, List.of(MainEffect.artifactMainEffect.ATK));
        mainEffectTable.put(Slot.artifactSlot.LOZENGE, List.of(MainEffect.artifactMainEffect.HP, MainEffect.artifactMainEffect.ATK, MainEffect.artifactMainEffect.DEF, MainEffect.artifactMainEffect.CRI, MainEffect.artifactMainEffect.CRIDMG));
        mainEffectTable.put(Slot.artifactSlot.CLOVER, List.of(MainEffect.artifactMainEffect.HP, MainEffect.artifactMainEffect.ATK, MainEffect.artifactMainEffect.DEF, MainEffect.artifactMainEffect.VIT, MainEffect.artifactMainEffect.LUK));
        mainEffectTable.put(Slot.artifactSlot.CUSHION, List.of(MainEffect.artifactMainEffect.HP, MainEffect.artifactMainEffect.DEF, MainEffect.artifactMainEffect.VIT));
        mainEffectTable.put(Slot.artifactSlot.CRESCENT, List.of(MainEffect.artifactMainEffect.ATK, MainEffect.artifactMainEffect.DEF, MainEffect.artifactMainEffect.FIRE_DMG_BONUS, MainEffect.artifactMainEffect.WATER_DMG_BONUS, MainEffect.artifactMainEffect.NATURE_DMG_BONUS));
    }

    public static Map<Slot.artifactSlot, List<MainEffect.artifactMainEffect>> ExceptionMainEffectTable = new HashMap<>();
    static {//MainEffect固定を持つアーティファクト用の抽選テーブル
        ExceptionMainEffectTable.put(Slot.artifactSlot.PEAR, List.of(MainEffect.artifactMainEffect.HP));
        ExceptionMainEffectTable.put(Slot.artifactSlot.OVAL, List.of(MainEffect.artifactMainEffect.ATK));
        ExceptionMainEffectTable.put(Slot.artifactSlot.LOZENGE, List.of(MainEffect.artifactMainEffect.HP));
        ExceptionMainEffectTable.put(Slot.artifactSlot.CLOVER, List.of(MainEffect.artifactMainEffect.VIT));
        ExceptionMainEffectTable.put(Slot.artifactSlot.CUSHION, List.of(MainEffect.artifactMainEffect.DEF));
        ExceptionMainEffectTable.put(Slot.artifactSlot.CRESCENT, List.of(MainEffect.artifactMainEffect.ATK));
    }


    public static HashMap<MainEffect.artifactMainEffect,Double> mainEffectInitialValue = new HashMap<>();
    static {//各ステータスの初期値のテーブル
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.HP,0.05);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.ATK,0.1);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.DEF,0.3);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.VIT,0.03);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.LUK,5.0);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.CRI,0.03);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.CRIDMG,0.06);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.FIRE_DMG_BONUS,0.05);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.WATER_DMG_BONUS,0.05);
        mainEffectInitialValue.put(MainEffect.artifactMainEffect.NATURE_DMG_BONUS,0.05);
    }


    public static HashMap<MainEffect.artifactMainEffect, Double> mainEffectBaseGrowthRate = new HashMap<>();
    static {
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.HP,0.025);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.ATK,0.04);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.DEF,0.03);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.VIT,0.01);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.LUK,1.0);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.CRI,0.01);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.CRIDMG,0.02);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.FIRE_DMG_BONUS,0.015);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.WATER_DMG_BONUS,0.015);
        mainEffectBaseGrowthRate.put(MainEffect.artifactMainEffect.NATURE_DMG_BONUS,0.015);
    }

    public static Double[] mainEffectExceptionGrowthRate = new Double[30];
    static {//成長率にボーナスつける時にどうぞ
        Arrays.fill(mainEffectExceptionGrowthRate,1.0);
        for(int i = 0;i<mainEffectExceptionGrowthRate.length;i++){
            if(i < 5 || i > mainEffectExceptionGrowthRate.length -5){
                mainEffectExceptionGrowthRate[i] = 1.5;//最初と最後の5回に1.5倍ボーナス
            }
        }
    }

}
