package io.github.itokagimaru.artifact.artifact.items;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SpecialItems {
    public ItemStack artifactHolder(){
        ItemStack stack = new ItemStack(Material.WOODEN_HOE);
        stack.editMeta(meta -> {
            meta.setItemModel(NamespacedKey.minecraft("bundle"));
            meta.customName(Component.text("魔法の麻袋"));
            meta.lore(List.of(
                    Component.text("中に\"アーティファクト\"を"),
                    Component.text("入れる事で"),
                    Component.text("真の力を引き出してくれる"),
                    Component.text("不思議な麻袋"),
                    Component.text("一説では伝説の\"錬金術師\"が作ったものと"),
                    Component.text("されている")
            ));
            meta.setMaxStackSize(1);
        });
        return stack;
    }

    public static final NamespacedKey UNIDENTIFIED_ARTIFACT_KEY = new NamespacedKey("artifact", "unidentified_artifact");
    public static final NamespacedKey APPRAISED_ARTIFACT_KEY = new NamespacedKey("artifact", "appraised_artifact");
    public static final NamespacedKey TIER_KEY = new NamespacedKey("artifact", "tier");

    public static ItemStack getUnidentifiedArtifact(String internalName) {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        item.editMeta(meta -> {
            meta.customName(Component.text("未鑑定のアーティファクト")
                    .color(NamedTextColor.BLUE)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("<%s>".formatted(SeriesRegistry.getSeries(internalName).getSeriesName()))
                            .color(NamedTextColor.WHITE)
                            .decorate(TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false))
            );
            meta.lore(List.of(
                    Component.text("鑑定されていないアーティファクト。")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("ここから削り出しと鑑定を行う必要がある")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("投げて遊ぶ事もできる")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.STRIKETHROUGH, true)
            ));
            meta.setMaxStackSize(1);
            meta.getPersistentDataContainer().set(UNIDENTIFIED_ARTIFACT_KEY, PersistentDataType.STRING, internalName);
        });
        return item;
    }

    public static boolean isUnidentifiedArtifact(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(UNIDENTIFIED_ARTIFACT_KEY, PersistentDataType.STRING);
    }

    public static ItemStack getAppraisedArtifact(String internalName, String tier) {
        ItemStack item = new ItemStack(Material.FLINT);
        item.editMeta(meta -> {
            meta.customName(Component.text("鑑定済みのアーティファクト")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("<%s>".formatted(SeriesRegistry.getSeries(internalName).getSeriesName()))
                            .color(NamedTextColor.WHITE)
                            .decorate(TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false))
            );
            meta.lore(List.of(
                    Component.text("鑑定されたアーティファクト (Tier:%s)".formatted(tier))
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("これを加工して初めて装備可能になる")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.setMaxStackSize(1);
            meta.getPersistentDataContainer().set(APPRAISED_ARTIFACT_KEY, PersistentDataType.STRING, internalName);
            meta.getPersistentDataContainer().set(TIER_KEY, PersistentDataType.STRING, tier);
        });
        return item;
    }

    public static boolean isAppraisedArtifact(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(APPRAISED_ARTIFACT_KEY, PersistentDataType.STRING);
    }

    public static final NamespacedKey AUGMENT_KEY = new NamespacedKey("artifact", "augment");

    public static ItemStack getAugment() {
        ItemStack item = new ItemStack(Material.DISC_FRAGMENT_5);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("オーグメント").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("アーティファクト強化の補助素材。").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("強化時にセットすると成功率が").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("1%").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                        .append(Component.text("上昇する。").color(NamedTextColor.GRAY))
        ));

        meta.getPersistentDataContainer().set(AUGMENT_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isAugment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(AUGMENT_KEY, PersistentDataType.BYTE);
    }
}
