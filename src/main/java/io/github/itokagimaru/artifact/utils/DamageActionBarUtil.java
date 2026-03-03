package io.github.itokagimaru.artifact.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class DamageActionBarUtil {
    private static final Map<Character, Integer> FONT_WIDTH = new HashMap<>();

    static {
        String onePx = "!'|:,.;i";
        for (char c : onePx.toCharArray()) FONT_WIDTH.put(c, 1);

        String twoPx = "l`";
        for (char c : twoPx.toCharArray()) FONT_WIDTH.put(c, 2);

        String threePx = "tI[]{}() ";
        for (char c : threePx.toCharArray()) FONT_WIDTH.put(c, 3);

        String fourPx = "fk<>*\"";
        for (char c : fourPx.toCharArray()) FONT_WIDTH.put(c, 4);

        for (char c = '0'; c <= '9'; c++) FONT_WIDTH.put(c, 5);
        for (char c = 'a'; c <= 'z'; c++) FONT_WIDTH.put(c, 5);
        for (char c = 'A'; c <= 'Z'; c++) FONT_WIDTH.put(c, 5);
    }

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

        String leftBlockText = attackerName + " >> " + leftHalf;
        String rightBlockText = rightHalf + " >> " + damagedName;

        int leftWidth = calculateWidth(leftBlockText, true);
        int rightWidth = calculateWidth(rightBlockText, true);

        int diff = Math.abs(leftWidth - rightWidth);
        int spaceWidth = getCharWidth(' ', true);
        int spaces = diff / spaceWidth;

        String padding = " ".repeat(spaces/2);

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

        if (leftWidth < rightWidth) {
            result = Component.text(padding)
                    .append(Component.text(padding))
                    .append(attackerPart)
                    .append(arrow)
                    .append(leftDamage)
                    .append(centerDamage)
                    .append(rightDamage)
                    .append(arrow)
                    .append(damagedPart);
        } else {
            result = attackerPart
                    .append(arrow)
                    .append(leftDamage)
                    .append(centerDamage)
                    .append(rightDamage)
                    .append(arrow)
                    .append(damagedPart)
                    .append(Component.text(padding));
        }

        return result;
    }

    private static int calculateWidth(String text, boolean bold) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += getCharWidth(c, bold) + 1;
        }
        return width;
    }

    private static int getCharWidth(char c, boolean bold) {
        int base = FONT_WIDTH.getOrDefault(c, 5);
        if (bold && c != ' ') base += 1;
        return base;
    }
}