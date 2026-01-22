package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.data.ItemData;
import org.bukkit.Material;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactEquipMenu;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemUseListener implements Listener {
    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (item == null) return;
        if (item.getType() != Material.WOODEN_HOE) return;
        if (item.getItemMeta().hasItemModel()) {
            player.sendMessage("you use skill!!");
            if (ItemData.IS_SKILL_ITEM.get(item) == (byte) 1) EffectStack.runByTrigger(TriggerType.triggerType.ON_SKILL_USE, player.getUniqueId(), event);
        }
        if (SpecialItems.isArtifactHolder(item)) {
            // デフォルトの動作をキャンセル
            event.setCancelled(true);
            // ArtifactEquipMenuを開く
            new ArtifactEquipMenu(player).open(player);
        }
    }
}
