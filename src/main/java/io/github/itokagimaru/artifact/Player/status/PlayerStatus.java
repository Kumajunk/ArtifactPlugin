package io.github.itokagimaru.artifact.Player.status;

import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.utils.BaseGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.*;


public class PlayerStatus {
    public enum playerStatus {
        HP(0,"HP"),
        ATK(1,"攻撃力"),
        DEF(2,"防御力"),
        AGI(3,"移動速度"),
        VIT(4,"回復補正"),
        CRI(5,"会心率"),
        CRIDMG(6,"会心ダメージ"),
        FIRE_DMG_BONUS(7,"属性ダメージ -火-"),
        WATER_DMG_BONUS(8,"属性ダメージ -水-"),
        NATURE_DMG_BONUS(9,"属性ダメージ -木-"),
        FIRE_DMG_REDUCE(10,"属性軽減 -火-"),
        WATER_DMG_REDUCE(11,"属性軽減 -水-"),
        NATURE_DMG_REDUCE(12,"属性軽減 -木-"),;
        final String text;
        final int id;
        playerStatus(int id, String text){
            this.id = id;
            this.text = text;
        }
        public static final HashMap<Integer, playerStatus> statusHashMap = new HashMap<>();
        public static int statusSize = 0;
        static {
            for (playerStatus status : values()){
                statusHashMap.put(status.id,status);
                statusSize++;
            }
        }
        public static playerStatus fromId(int value){
            return statusHashMap.get(value);
        }
    }
    ElementStatus baseElement = new ElementStatus();
    ElementStatus buffElement = new ElementStatus();
    ElementStatus weaponElement = new ElementStatus();
    public ElementStatus.Element getBaseElement() {
        return baseElement.getElement();
    }
    public void setBaseElement(ElementStatus.Element baseElement) {
        this.baseElement.setElement(baseElement);
    }
    public ElementStatus.Element getBuffElement() {
        return buffElement.getElement();
    }
    public void setBuffElement(ElementStatus.Element buffElement) {
        this.buffElement.setElement(buffElement);
    }
    public ElementStatus.Element getWeaponElement(){
        return weaponElement.getElement();
    }
    public void setWeaponElement(ElementStatus.Element element){
        this.weaponElement.setElement(element);
    }

    public ElementStatus.Element getElement(){
        if(weaponElement.getElement() != ElementStatus.Element.NULL) return weaponElement.getElement();
        if(buffElement.getElement() != ElementStatus.Element.NULL) return buffElement.getElement();
        return baseElement.getElement();
    }
    private final Map<playerStatus, Double> baseStatus = new EnumMap<playerStatus, Double>(playerStatus.class);
    {   baseStatus.put(playerStatus.HP, 20.0);
        baseStatus.put(playerStatus.ATK, 10.0);
        baseStatus.put(playerStatus.DEF, 10.0);
        baseStatus.put(playerStatus.AGI, 0.1);
        baseStatus.put(playerStatus.VIT, 0.00);
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

    public List<String> getAllStatusText(){
        PlayerStatus playerStatus = this;
        List<String> lore = new ArrayList<>();
        for (PlayerStatus.playerStatus status : PlayerStatus.playerStatus.values()) {
            double displayValue = playerStatus.getStatus(
                    status
            );
            String suffix = "";

            boolean isBaseStat =
                    status == PlayerStatus.playerStatus.HP ||
                            status == PlayerStatus.playerStatus.ATK ||
                            status == PlayerStatus.playerStatus.DEF;

            if (!isBaseStat) {
                if (status == PlayerStatus.playerStatus.AGI) {
                    displayValue *= 1000;
                } else {
                    displayValue *= 100;
                }

                suffix = "%";
            }


            lore.add("§7"+status.text + ": §f"
                    + doubleToString(displayValue)
                    + suffix);
        }
        return lore;
    }


    public List<Component> getAllStatusComponents(){
        PlayerStatus playerStatus = this;
        List<Component> lore = new ArrayList<>();
        for (PlayerStatus.playerStatus status : PlayerStatus.playerStatus.values()) {
            double displayValue = playerStatus.getStatus(
                    status
            );
            String suffix = "";

            boolean isBaseStat =
                    status == PlayerStatus.playerStatus.HP ||
                            status == PlayerStatus.playerStatus.ATK ||
                            status == PlayerStatus.playerStatus.DEF;

            if (!isBaseStat) {
                if (status == PlayerStatus.playerStatus.AGI) {
                    displayValue *= 1000;
                } else {
                    displayValue *= 100;
                }

                suffix = "%";
            }

            lore.add(Component.text(status.text + ": ").color(NamedTextColor.GRAY).append(Component.text(doubleToString(displayValue)
                    + suffix)).color(NamedTextColor.WHITE));
        }
        return lore;
    }



    private String doubleToString(double d){
        return String.format("%.2f", d);
    }

}
