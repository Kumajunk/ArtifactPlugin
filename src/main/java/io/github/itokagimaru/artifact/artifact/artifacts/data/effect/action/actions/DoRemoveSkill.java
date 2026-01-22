package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.data.ItemData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class DoRemoveSkill extends Action {
    String key;
    public DoRemoveSkill(String key){
        this.key = key;
    }
    @Override
    public void run(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        Inventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        for(int i = 0; i < items.length; i++){
            ItemStack item = items[i];
            if(item == null || item.getType() == Material.AIR) continue;
            if(item.hasItemMeta()){
                String skillKey = ItemData.SKILL_KEY.get(item);
                if(key.equals(skillKey)){
                    inventory.setItem(i, null);
                }
            }
        }
    }
}
