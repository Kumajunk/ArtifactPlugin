package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.EquipPdc;
import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;

import static io.github.itokagimaru.artifact.artifact.EquipPdc.loadFromPdc;
import static io.github.itokagimaru.artifact.artifact.EquipPdc.saveToPdc;

public class ArtifactEquipMenu extends BaseGui {
    private final Player player;

    public ArtifactEquipMenu(Player player) {
        super(9, "Artifact Equip Menu");
        this.player = player;
        setupMenu();
        setupPlayerInventoryHandler();
    }

    public void setupMenu() {
        for (Slot.artifactSlot slot : Slot.artifactSlot.values()) {
            BaseArtifact artifact = loadFromPdc(player, slot);
            if (artifact == null) {
                setItem(slot.getId, new ItemBuilder()
                        .setMaterial(Material.BARRIER)
                        .setName("§a"+slot.getSlotName+" 枠")
                        .addLore("§7"+slot.getSlotName+"のアーティファクトをクリックして装備")
                );
            } else {
                setItem(slot.getId, ArtifactToItem.convert(artifact), player -> {
                    player.sendMessage(slot.getSlotName+" の装備を解除しました");
                    EquipPdc.removeFromPdc(player, slot);
                    ArtifactMain.getStashManager().giveOrStash(player.getUniqueId(), JsonConverter.serializeArtifact(artifact), "装備解除");
                    new ArtifactEquipMenu(player).open(player);
                });
            }
        }

    }

    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            // 空のスロットは無視
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            // アーティファクトかどうか判定
            Optional<BaseArtifact> artifact = ItemToArtifact.convert(item);
            if (artifact.isEmpty()) {
                player.sendMessage("§cこのアイテムはアーティファクトではありません");
                return;
            }


            BaseArtifact isAlreadyEquip = EquipPdc.loadFromPdc(player, artifact.get().getSlot());
            if (isAlreadyEquip != null) {
                player.sendMessage("§c先に現在の装備を外してください");
                return;
            }
            saveToPdc(player, artifact.get().getSlot(), artifact.get());
            player.getInventory().clear(slot);
            player.sendMessage("§a装備しました！");
            new ArtifactEquipMenu(player).open(player);
        });
    }

}
