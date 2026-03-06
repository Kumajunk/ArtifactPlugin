package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.data.ItemData;
import io.github.itokagimaru.artifact.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class DoGiveSkill extends Action {
    ItemStack skillItem = new ItemStack(Material.WOODEN_HOE);
    public DoGiveSkill(String key, String itemName, String model, List<Component> lore){
        skillItem.editMeta(meta -> {
            meta.customName(Utils.parseLegacy(itemName.replace('&', '§')));
            meta.setItemModel( NamespacedKey.minecraft(model));
            meta.lore(lore);
        });
        ItemData.IS_SKILL_ITEM.set(skillItem, (byte) 1);
        ItemData.SKILL_KEY.set(skillItem, key);
    }
    @Override
    public void run(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        Inventory inventory = player.getInventory();
        inventory.addItem(skillItem);
    }
}
