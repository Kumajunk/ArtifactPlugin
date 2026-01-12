package io.github.itokagimaru.artifact.Player.status;

import java.util.*;


public class PlayerStatus {
    public enum playerStatus {
        HP(0,"HitPoint"),
        ATK(1,"Attack"),
        DEF(2,"Defense"),
        LUK(3,"Luck"),
        VIT(4,"Vitality"),
        CRI(5,"CriticalRate"),
        CRIDMG(6,"CriticalDamage"),
        FIRE_DMG_BONUS(7,"FireDamageBonus"),
        WATER_DMG_BONUS(8,"WaterDamageBonus"),
        NATURE_DMG_BONUS(9,"NatureDamageBonus"),
        FIRE_DMG_REDUCE(10,"FireDamageReduceRate"),
        WATER_DMG_REDUCE(11,"WaterDamageReduceRate"),
        NATURE_DMG_REDUCE(12,"NatureDamageReduceRate");
        playerStatus(int id, String text){

        }
    }
    private final Map<playerStatus, Double> baseStatus = new EnumMap<playerStatus, Double>(playerStatus.class);
    {   baseStatus.put(playerStatus.HP, 20.0);
        baseStatus.put(playerStatus.ATK, 10.0);
        baseStatus.put(playerStatus.DEF, 50.0);
        baseStatus.put(playerStatus.LUK, 1.0);
        baseStatus.put(playerStatus.VIT, 0.05);
        baseStatus.put(playerStatus.CRI, 0.05);
        baseStatus.put(playerStatus.CRIDMG, 0.5);
        baseStatus.put(playerStatus.FIRE_DMG_BONUS, 0.0);
        baseStatus.put(playerStatus.WATER_DMG_BONUS, 0.0);
        baseStatus.put(playerStatus.NATURE_DMG_BONUS, 0.0);
        baseStatus.put(playerStatus.FIRE_DMG_REDUCE, 0.0);
        baseStatus.put(playerStatus.WATER_DMG_REDUCE, 0.0);
        baseStatus.put(playerStatus.NATURE_DMG_REDUCE, 0.0);
    }
    private final ModifierStack modifierStack = new ModifierStack();

    public void addModifier(StatusModifier modifier) {
        modifierStack.add(modifier);
    }

    public boolean removeModifier(UUID modifierId) {
        return modifierStack.remove(modifierId);
    }

    public ModifierStack getModifierStack() {
        return modifierStack;
    }

    public StatusModifier findById(UUID id) {
        return modifierStack.findById(id);
    }

    public double getStatus(playerStatus status){
        List<StatusModifier> modifiers = modifierStack.getByStat(status);
        double addTypeModifier = 0.0;
        double multiplyTypeModifier = 1.0;
        for (StatusModifier modifier : modifiers){
            switch (modifier.getType()){
                case ADD -> {
                    addTypeModifier += modifier.getValue();
                }
                case MULTIPLY -> {
                    multiplyTypeModifier += modifier.getValue();
                }
            }
        }
        return (baseStatus.get(status) + addTypeModifier) * multiplyTypeModifier;
    }
}
