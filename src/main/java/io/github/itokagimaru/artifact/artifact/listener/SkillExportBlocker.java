package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.data.ItemData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SkillExportBlocker implements Listener {
    @EventHandler
    public void skillExportBlocker(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCursor();
        if (item == null || item.getType() == Material.AIR) return;

        if (ItemData.IS_SKILL_ITEM.get(item) != (byte) 1) return;
        if (event.getClickedInventory() == event.getView().getBottomInventory()) return;
        event.setCancelled(true);
    }
}
