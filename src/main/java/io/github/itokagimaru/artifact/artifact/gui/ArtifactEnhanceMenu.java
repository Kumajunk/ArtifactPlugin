package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArtifactEnhanceMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    private static final int BASE_SLOT = 13;
    private static final int MATERIAL_SLOT_1 = 29;
    private static final int MATERIAL_SLOT_2 = 31;
    private static final int MATERIAL_SLOT_3 = 33;
    private static final int EXECUTE_BUTTON = 49;
    private final ItemStack enhanceTarget;
    private final List<ItemStack> enhanceMaterials;

    public ArtifactEnhanceMenu() {
        this(null, new ArrayList<>());
    }

    public ArtifactEnhanceMenu(ItemStack enhanceTarget, List<ItemStack> enhanceMaterials){
        super(GUI_SIZE, "§lアーティファクト強化");
        this.enhanceTarget = enhanceTarget;
        this.enhanceMaterials = enhanceMaterials;
        setupGui();
        setupPlayerInventoryHandler();
    }

    private void setupPlayerInventoryHandler(){
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (!ItemToArtifact.isArtifact(item)) {
                player.sendMessage("§cこれはアーティファクトではありません！");
                return;
            }
            if (enhanceTarget == null) {
                BaseArtifact artifact = ItemToArtifact.convert(item).orElse(null);
                if (artifact == null) {
                    player.sendMessage("§cアーティファクトの情報が取得できませんでした！");
                    return;
                }
                if (artifact.getExStatus() != null) {
                    for (ExceptionStatus.artifactExceptionStatus status : artifact.getExStatus()) {
                        if (status == ExceptionStatus.artifactExceptionStatus.CANNOT_ENHANCE) {
                            player.sendMessage("§cこのアーティファクトは強化できません！");
                            return;
                        }
                    }
                }
                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                new ArtifactEnhanceMenu(item, new ArrayList<>()).open(player);
                return;
            }
            if (!enhanceMaterials.contains(item) && enhanceMaterials.stream().count() <= 2) {
                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                enhanceMaterials.add(item);
                new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials).open(player);
                return;
            }
        });
    }

    private void setupGui(){
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        if (enhanceTarget != null) {
            setReturnItem(BASE_SLOT, enhanceTarget, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(enhanceTarget);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactEnhanceMenu(null, enhanceMaterials).open(player);
            });
        } else {
            setItem(BASE_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c強化するアーティファクトをセットしてください"));
        }

        int[] materialSlots = {29, 31, 33};
        for (int slot : materialSlots) {
            int index = (slot - 29) / 2;
            if (enhanceMaterials.size() > index) {
                ItemStack material = enhanceMaterials.get(index);
                setReturnItem(slot, material, player -> {
                    Map<Integer, ItemStack> remaining = player.getInventory().addItem(material);
                    if (!remaining.isEmpty()) {
                        remaining.values().forEach(i ->
                                player.getWorld().dropItemNaturally(player.getLocation(), i)
                        );
                    }
                    enhanceMaterials.remove(index);
                    new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials).open(player);
                });
            } else {
                setItem(slot, new ItemBuilder()
                        .setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName("§e強化素材をセットしてください"));
            }
        }

        setItem(EXECUTE_BUTTON, new ItemBuilder()
                .setMaterial(Material.ANVIL)
                .setName("§a強化を実行する")
                .setClickAction(ClickType.LEFT, player -> {
                    if (enhanceTarget == null) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§c強化するアーティファクトがセットされていません！");
                        return;
                    }
                    if (enhanceMaterials.isEmpty()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§c強化素材がセットされていません！");
                        return;
                    }
                    if (enhanceMaterials.size() <= 2) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§c強化素材が不足しています！");
                        return;
                    }
                    Optional<BaseArtifact> artifact = ItemToArtifact.convert(enhanceTarget);
                    if (artifact.isEmpty()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§cアーティファクトの情報が取得できませんでした！");
                        return;
                    }
                    BaseArtifact baseArtifact = artifact.get();
                    int lv = baseArtifact.getLv();
                    if (lv == 30) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§cアーティファクトのレベルは最大です！");
                        return;
                    }
                    double currentProb = 1 - ((1 - 0.6) / (30 - 1.0)) * lv;
                    boolean isSuccess = Math.random() < currentProb;
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 0.5f);
                    if (isSuccess) {
                        baseArtifact.performEnhance();
                    } else {
                        player.sendMessage("§cアーティファクトの強化に失敗しました...");
                    }
                    ItemStack enhancedItem = ArtifactToItem.convert(baseArtifact);
                    new ArtifactEnhanceMenu(enhancedItem, new ArrayList<>()).open(player);
                })
        );
    }
}
