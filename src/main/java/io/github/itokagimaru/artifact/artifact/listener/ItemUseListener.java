package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.artifact.gui.ArtifactEquipMenu;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemUseListener implements Listener {

    /**
     * プレイヤーがアイテムを使用した際のハンドラ
     * ArtifactHolderを右クリックした場合、ArtifactEquipMenuを開く
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 右クリックのみ処理
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // ArtifactHolderかどうかを判定
        if (SpecialItems.isArtifactHolder(item)) {
            // デフォルトの動作をキャンセル
            event.setCancelled(true);
            // ArtifactEquipMenuを開く
            new ArtifactEquipMenu(player).open(player);
        }
    }
}
