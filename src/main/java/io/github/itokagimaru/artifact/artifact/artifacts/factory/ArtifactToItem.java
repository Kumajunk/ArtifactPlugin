package io.github.itokagimaru.artifact.artifact.artifacts.factory;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.data.ItemData;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Arrays;
import java.util.List;

public class ArtifactToItem {
    public static ItemStack convert(BaseArtifact artifact){
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemData.UUID.set(stack, artifact.getUUID().toString());
        ItemData.TIER.set(stack, artifact.getTier().getId);
        ItemData.SLOT.set(stack, artifact.getSlot().getId);
        ItemData.SERIES_KEY.set(stack, artifact.getSeries().getSeriesName());
        ItemData.LV.set(stack, artifact.getLv());
        ItemData.MAIN_ID.set(stack, artifact.getMainEffect().getId);
        ItemData.MAIN_VALUE.set(stack, ByteArrayConverter.toByte(artifact.getMainEffectValue()));


        int[] subEffects = new int[4];
        double[] subEffectsValue = new double[4];
        Arrays.fill(subEffects, -1);
        Arrays.fill(subEffectsValue, 0);
        int i = 0;
        for (int j = 0; j < artifact.getSubEffects().length; j++){
            if(artifact.getSubEffects()[j] == null) continue;
            subEffects[i] = artifact.getSubEffects()[j].getId;
            subEffectsValue[i] = artifact.getSubEffectsValue()[j];
            i++;
        }
        ItemData.SUB_ID.set(stack, subEffects);
        ItemData.SUB_VALUE.set(stack, ByteArrayConverter.toByte(subEffectsValue));

        stack.editMeta(itemMeta -> {
            itemMeta.setMaxStackSize(1);
            itemMeta.setItemModel(NamespacedKey.minecraft(artifact.getSeries().getModel()));
            CustomModelDataComponent cmd = itemMeta.getCustomModelDataComponent();
            cmd.setFloats(List.of((float) artifact.getSlot().getId));
            itemMeta.setCustomModelDataComponent(cmd);
            itemMeta.customName(makeName(artifact));
            itemMeta.lore(makeLore(artifact));
        });
        return stack;
    }
    private static Component makeName(BaseArtifact artifact){
        return Component.text("ArtifactSeries <").color(NamedTextColor.GRAY).append(Component.text(artifact.getSeries().getSeriesName()).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD).append(Component.text(">").color(NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false);
    }
    private static List<Component> makeLore(BaseArtifact artifact){
        List<Component> lore = new java.util.ArrayList<>(List.of(
                Component.text("Tier: ").color(NamedTextColor.GRAY).append(Component.text(artifact.getTier().getText).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false),
                Component.text("Lv: ").color(NamedTextColor.GRAY).append(Component.text("+" + artifact.getLv()).color(NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
                Component.text("Cut: ").color(NamedTextColor.GRAY).append(Component.text(artifact.getSlot().getSlotName).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false),
                Component.text("-------------------------").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("MainEffect").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(artifact.getMainEffect().getText).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD).append(Component.text(" +").color(NamedTextColor.WHITE).append(Component.text((double) artifact.getMainEffectValue() *getRate(artifact.getMainEffect().getAddType)).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD).append(Component.text(artifact.getMainEffect().getAddTypeText).color(NamedTextColor.WHITE)))).decoration(TextDecoration.ITALIC, false),
                Component.text("-------------------------").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("SubEffect").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        for (int i = 0; i < artifact.getSubEffects().length; i++){
            if (artifact.getSubEffects()[i] == null) continue;
            lore.addLast(Component.text(artifact.getSubEffects()[i].getText).color(NamedTextColor.GRAY).append(Component.text(" +").color(NamedTextColor.WHITE).append(Component.text((double) artifact.getSubEffectsValue()[i]*100).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD).append(Component.text("%").color(NamedTextColor.WHITE)))).decoration(TextDecoration.ITALIC, false));
        }
        lore.addLast(Component.text("-------------------------").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("setEffect").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("2set:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.addAll(artifact.getSeries().getTwoSetDescription());
        lore.add(Component.text("4set:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.addAll(artifact.getSeries().getFourSerDescription());
        lore.addLast(Component.text("-------------------------").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.addAll(artifact.getSeries().getFlavorText());
        lore.addLast(Component.text("-------------------------").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        for (ExceptionStatus.artifactExceptionStatus status : ExceptionStatus.artifactExceptionStatus.values()){
            if (ExceptionStatus.isHaveExceptionStatus(artifact.getSeries(),status)){
                lore.addLast(Component.text(status.getDescription).color(NamedTextColor.RED));
            }
        }
        return lore;
    }

    private static int getRate(MainEffect.valueType valType){
        if (valType == MainEffect.valueType.MULTIPLY){
            return 100;
        }
        return 1;
    }
}
