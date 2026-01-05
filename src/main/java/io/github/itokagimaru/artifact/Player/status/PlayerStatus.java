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
        NATURE_DMG_BONUS(9,"NatureDamageBonus");
        playerStatus(int id, String text){

        }
    }
    Map<playerStatus, Double> BaseStatus = new EnumMap<>(Map.of(
            playerStatus.HP, 2.0,
            playerStatus.ATK, 1.0,
            playerStatus.DEF, 5.0,
            playerStatus.LUK, 1.0,
            playerStatus.VIT, 5.0,
            playerStatus.CRI, 5.0,
            playerStatus.CRIDMG, 50.0,
            playerStatus.FIRE_DMG_BONUS, 0.0,
            playerStatus.WATER_DMG_BONUS, 0.0,
            playerStatus.NATURE_DMG_BONUS, 0.0
    ));
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
        double multiplyTypeModifier = 0.0;
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
        return (BaseStatus.get(status) + addTypeModifier) * multiplyTypeModifier;
    }
}
