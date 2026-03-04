package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInventoryClickListener implements Listener {
    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey("artifact", "stat_viewer");
        Byte flag = meta.getPersistentDataContainer()
                .get(key, PersistentDataType.BYTE);
        if (flag == null || flag != 1) return;
        if(player.getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
        Inventory clicked = event.getClickedInventory();
        if (clicked instanceof PlayerInventory) {
            clicked.setItem(event.getSlot(), SpecialItems.getStatViewer(player));
        }
    }
}
