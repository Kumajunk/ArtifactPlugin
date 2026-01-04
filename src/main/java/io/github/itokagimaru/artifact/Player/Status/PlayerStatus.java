package io.github.itokagimaru.artifact.Player.Status;

import java.util.*;


public class PlayerStatus {
    public enum status{
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
        status(int id, String text){

        }
    }
    Map<status, Integer> BaseStatus = new EnumMap<>(Map.of(
            status.HP, 20,
            status.ATK, 10,
            status.DEF, 50,
            status.LUK, 1,
            status.VIT, 5,
            status.CRI, 50,
            status.CRIDMG, 500,
            status.FIRE_DMG_BONUS, 0,
            status.WATER_DMG_BONUS, 0,
            status.NATURE_DMG_BONUS, 0
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

    public Optional<StatusModifier> findById(UUID id) {
        return Optional.ofNullable(modifierStack.get(id));
    }
}
