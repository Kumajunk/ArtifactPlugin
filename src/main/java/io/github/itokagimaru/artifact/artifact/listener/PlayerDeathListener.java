package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.artifact.event.EquipBrokeEvent;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import io.github.itokagimaru.artifact.artifact.EquipPdc;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import org.bukkit.Bukkit;

/**
 * プレイヤーデス時にタグ付きアイテムを消すリスナー
 */
public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // 装備中のアーティファクトの耐久を現在値の25%減少させる
        for (Slot.artifactSlot slot : Slot.artifactSlot.values()) {
            BaseArtifact artifact = EquipPdc.loadFromPdc(player, slot);
            if (artifact != null) {
                int currentDurability = artifact.getDurability();
                if (currentDurability > 0) {
                    int maxDurability = artifact.getMaxDurability();
                    int penalty = (int) Math.ceil(maxDurability * 0.25);
                    int newDurability = Math.max(0, currentDurability - penalty);
                    artifact.setDurability(newDurability);
                    EquipPdc.saveToPdc(player, slot, artifact);
                    
                    if (newDurability == 0) {
                        Bukkit.getPluginManager().callEvent(new EquipBrokeEvent(player, artifact, slot));
                    }
                }
            }
        }
        
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
