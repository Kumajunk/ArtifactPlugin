package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * プレイヤーデス時にタグ付きアイテムを消すリスナー
 */
public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // KeepInventoryの場合はインベントリから直接削除
        if (event.getKeepInventory()) {
            int removedCount = 0;
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (SpecialItems.hasRemoveOnDeathTag(item)) {
                    player.getInventory().setItem(i, null);
                    removedCount++;
                }
            }
            if (removedCount > 0) {
                player.sendMessage("§c" + removedCount + "個のアイテムがデスにより消失しました");
            }
            return;
        }
        
        // ドロップアイテムからタグ付きアイテムを削除
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        int removedCount = 0;
        
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (SpecialItems.hasRemoveOnDeathTag(item)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            player.sendMessage("§c" + removedCount + "個のアイテムがデスにより消失しました");
        }
    }
}
