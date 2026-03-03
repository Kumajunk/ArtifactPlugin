package io.github.itokagimaru.artifact.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class DamageActionBarUtil {

    private DamageActionBarUtil() {}

    public static Component buildCenteredDamageBar(
            Entity attacker,
            Entity damaged,
            String finalDmg,
            NamedTextColor damageColor,
            boolean criFlag
    ) {

        String attackerName = attacker.getName();
        String damagedName = damaged.getName();
        int len = finalDmg.length();
        int mid = len / 2;

        String leftHalf;
        String centerChar = "";
        String rightHalf;

        if (len % 2 == 0) {
            leftHalf = finalDmg.substring(0, mid);
            rightHalf = finalDmg.substring(mid);
        } else {
            leftHalf = finalDmg.substring(0, mid);
            centerChar = finalDmg.substring(mid, mid + 1);
            rightHalf = finalDmg.substring(mid + 1);
        }

        String padding = " ".repeat(150);

        Component attackerPart = Component.text(attackerName)
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);

        Component arrow = Component.text(" >> ")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.BOLD);

        Component leftDamage = Component.text(leftHalf)
                .color(damageColor)
                .decoration(TextDecoration.BOLD, criFlag);

        Component centerDamage = Component.text(centerChar)
                .color(damageColor)
                .decoration(TextDecoration.BOLD, criFlag);

        Component rightDamage = Component.text(rightHalf)
                .color(damageColor)
                .decoration(TextDecoration.BOLD, criFlag);

        Component damagedPart = Component.text(damagedName)
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);

        Component result;


        result = damagedPart
                .append(rightDamage)
                .append(arrow)
                .append(Component.text(padding))
                .append(attackerPart)
                .append(arrow)
                .append(leftDamage)
                .append(centerDamage)
                .append(rightDamage)
                .append(arrow)
                .append(damagedPart)
                .append(Component.text(padding))
                .append(attackerPart)
                .append(arrow)
                .append(leftDamage);
        return result;
    }
}