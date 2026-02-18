package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.Factory;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

import static io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot.artifactSlot.*;
import static io.github.itokagimaru.artifact.artifact.items.SpecialItems.isAppraisedArtifact;

public class ArtifactProcessMenu extends BaseGui {
    private ItemStack processingItem;
    private String processingItemId;
    private Tier.artifactTier tier;
    private final Slot.artifactSlot processingSlot;

    public ArtifactProcessMenu() {
        this(null, null,null, null);
    }

    public ArtifactProcessMenu(ItemStack processingItem, String processingItemId, Slot.artifactSlot processingSlot, Tier.artifactTier tier) {
        super(54, "§lアーティファクト加工");
        this.processingItem = processingItem;
        this.processingItemId = processingItemId;
        this.processingSlot = processingSlot;
        this.tier = tier;
        setupGui();
        setupPlayerInventoryClickHandler();
    }

    private void setupPlayerInventoryClickHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            if (isAppraisedArtifact(item)) {
                processingItem = item.clone();
                processingItemId = item.getPersistentDataContainer().get(SpecialItems.APPRAISED_ARTIFACT_KEY, PersistentDataType.STRING);
                tier = Tier.artifactTier.fromTier(item.getPersistentDataContainer().get(SpecialItems.TIER_KEY, PersistentDataType.STRING));
                item.setAmount(0);
                player.sendMessage("§a加工するアーティファクトをセットしました");
                new ArtifactProcessMenu(processingItem, processingItemId, null, tier).open(player);
                return;
            }
            player.sendMessage("§c加工できるアーティファクトをセットしてください");
        });
    }

    private void setupGui() {
        int APPRAISE_SLOT = 13;
        int PEAR_SLOT = 30;
        int OVAL_SLOT = 31;
        int LOZENGE_SLOT = 32;
        int CLOVER_SLOT = 39;
        int CUSHION_SLOT = 40;
        int CRESCENT_SLOT = 41;
        int CONFIRM_SLOT = 53;
        if (processingItem == null) {
            setItem(APPRAISE_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§a加工するアーティファクトをセットしてください"));
        } else {
            setReturnItem(APPRAISE_SLOT, processingItem, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(processingItem);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                processingItem = null;
                player.sendMessage("§a加工するアーティファクトを返却しました");
                new ArtifactProcessMenu().open(player);
            });
        }

        if (processingSlot == null) {
            setItem(PEAR_SLOT, buildSlotItem(PEAR), (player) -> {
                player.sendMessage("§aカットタイプをPearに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, PEAR, tier).open(player);
            });
            setItem(OVAL_SLOT, buildSlotItem(OVAL), (player) -> {
                player.sendMessage("§aカットタイプをOvalに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, OVAL, tier).open(player);
            });
            setItem(LOZENGE_SLOT, buildSlotItem(LOZENGE), (player) -> {
                player.sendMessage("§aカットタイプをLozengeに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, LOZENGE, tier).open(player);
            });
            setItem(CLOVER_SLOT, buildSlotItem(CLOVER), (player) -> {
                player.sendMessage("§aカットタイプをCloverに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, CLOVER, tier).open(player);
            });
            setItem(CUSHION_SLOT, buildSlotItem(CUSHION), (player) -> {
                player.sendMessage("§aカットタイプをCushionに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, CUSHION, tier).open(player);
            });
            setItem(CRESCENT_SLOT, buildSlotItem(CRESCENT), (player) -> {
                player.sendMessage("§aカットタイプをCrescentに選択しました");
                new ArtifactProcessMenu(processingItem, processingItemId, CRESCENT, tier).open(player);
            });
        } else {
            setItem(31, buildSlotItem(processingSlot), (player) -> {
                player.sendMessage("§aカットタイプの選択を解除しました");
                new ArtifactProcessMenu(processingItem, processingItemId, null, tier).open(player);
            });
        }

        if (processingItem != null && processingSlot != null) {
            setItem(CONFIRM_SLOT, new ItemBuilder()
                    .setMaterial(Material.EMERALD_BLOCK)
                    .setName("§a加工を確定する")
                    .setClickAction(ClickType.LEFT,player -> {
                        ItemStack item = new Factory().makeNewArtifact(SeriesRegistry.getSeries(processingItemId), processingSlot, tier);
                        Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                        if (!remaining.isEmpty()) {
                            remaining.values().forEach(i ->
                                    player.getWorld().dropItemNaturally(player.getLocation(), i)
                            );
                        }
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 2.0f);
                        player.sendMessage("§aアーティファクトの加工が完了しました");
                        player.getInventory().close();
                    })
            );
        }

    }

    private ItemStack buildSlotItem(Slot.artifactSlot slot) {
        ItemStack item = ItemStack.of(Material.PAPER);

        String name = switch (slot) {
            case PEAR -> "§aPear Cut";
            case OVAL -> "§aOval Cut";
            case LOZENGE -> "§aLozenge Cut";
            case CLOVER -> "§aClover Cut";
            case CUSHION -> "§aCushion Cut";
            case CRESCENT -> "§aCrescent Cut";
        };

        item.editMeta(meta -> {
            meta.customName(Utils.parseLegacy(name));
            meta.setItemModel(NamespacedKey.minecraft("default"));

            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of((float) slot.getId));
            meta.setCustomModelDataComponent(cmd);
        });

        return item;
    }
}
