package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.passive;

public class StatusEffect {
    public static enum statusEffect{
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
        statusEffect(int id, String statusName){

        }
    }
}
