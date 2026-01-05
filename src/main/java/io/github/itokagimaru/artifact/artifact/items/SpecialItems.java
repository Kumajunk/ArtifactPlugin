package io.github.itokagimaru.artifact.artifact.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

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
}
