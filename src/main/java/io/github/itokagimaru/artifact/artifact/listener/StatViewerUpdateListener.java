package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.config.UiConfig;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StatViewerUpdateListener implements Listener {
    UiConfig uiConfig = null;
    public StatViewerUpdateListener(UiConfig uiConfig) {
        this.uiConfig = uiConfig;
    }

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
            clicked.setItem(event.getSlot(), SpecialItems.getStatViewer(player, uiConfig));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        //ステータスを見える化アイテムが存在しなければつけます
        if(player.getInventory().getItem(39) != null) return;
        player.getInventory().setItem(39, SpecialItems.getStatViewer(player, uiConfig));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        //ステータスを見える化アイテムが存在しなければつけます
        if(player.getInventory().getItem(39) != null) return;
        player.getInventory().setItem(39, SpecialItems.getStatViewer(player, uiConfig));
    }
}
