package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.data.ItemData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerItemDropListener implements Listener {
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack dropItem = event.getItemDrop().getItemStack();
        if (dropItem == null) return;
        if (ItemData.IS_SKILL_ITEM.get(event.getItemDrop().getItemStack()) != (byte) 1) return;
        event.setCancelled(true);
    }
}
