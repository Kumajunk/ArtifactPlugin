package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArtifactEnhanceMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    private static final int BASE_SLOT = 13;
    private static final int[] MATERIAL_SLOTS = {29, 31, 33};
    private static final int AUGMENT_SLOT = 40; //
    private static final int EXECUTE_BUTTON = 49;
    private final ItemStack enhanceTarget;
    private final List<ItemStack> enhanceMaterials;
    private final ItemStack augment;

    public ArtifactEnhanceMenu() {
        this(null, new ArrayList<>(), null);
    }

    public ArtifactEnhanceMenu(ItemStack enhanceTarget, List<ItemStack> enhanceMaterials, ItemStack augment) {
        super(GUI_SIZE, "§lアーティファクト強化");
        this.enhanceTarget = enhanceTarget;
        this.enhanceMaterials = enhanceMaterials;
        this.augment = augment;
        setupGui();
        setupPlayerInventoryHandler();
    }

    private void setupPlayerInventoryHandler(){
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            if (SpecialItems.isAugment(item)) {
                if (augment == null) {
                    ItemStack newAugment = item.clone();
                    newAugment.setAmount(1);
                    item.setAmount(item.getAmount() - 1);
                    new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials, newAugment).open(player);
                } else {
                    if (augment.getAmount() == 64) {
                        player.sendMessage("§cオーグメントの上限に達しています！");
                        return;
                    }
                    augment.setAmount(augment.getAmount() + 1);
                    item.setAmount(item.getAmount() - 1);
                    new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials, augment).open(player);
                }
                return;
            }
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
                if (checkLevelCap(artifact)) {
                    player.sendMessage("§cこのアーティファクトはこれ以上強化できません！");
                    return;
                }
                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                new ArtifactEnhanceMenu(item, enhanceMaterials, augment).open(player);
                return;
            }
            if (!enhanceMaterials.contains(item)) {
                BaseArtifact artifact = ItemToArtifact.convert(item).orElse(null);
                BaseArtifact targetArtifact = ItemToArtifact.convert(enhanceTarget).orElse(null);
                if (artifact == null || targetArtifact == null) {
                    player.sendMessage("§cアーティファクトの情報が取得できませんでした！");
                    return;
                }
                if ((long) enhanceMaterials.size() < 3) {
                    if (!checkArtifactSeries(artifact, targetArtifact)) {
                        player.sendMessage("§c強化素材のシリーズが一致しません！");
                        return;
                    }
                    if (artifact.getLv() != 0) {
                        player.sendMessage("§c強化素材のレベルは0でなければいけません！");
                        return;
                    }
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                    enhanceMaterials.add(item);
                    new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials, augment).open(player);
                    return;
                } else {
                    player.sendMessage("§c強化素材の上限に達しています！");
                }
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
                new ArtifactEnhanceMenu(null, enhanceMaterials, augment).open(player);
            });
        } else {
            setItem(BASE_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c強化するアーティファクトをセットしてください"));
        }

        for (int slot : MATERIAL_SLOTS) {
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
                    new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials, augment).open(player);
                });
            } else {
                setItem(slot, new ItemBuilder()
                        .setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName("§e強化素材をセットしてください"));
            }
        }

        if (augment != null) {
            setReturnItem(AUGMENT_SLOT, augment, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(augment);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactEnhanceMenu(enhanceTarget, enhanceMaterials, null).open(player);
            });
        } else {
            setItem(AUGMENT_SLOT, new ItemBuilder()
                    .setMaterial(Material.ORANGE_STAINED_GLASS_PANE)
                    .setName("§6オーグメントをセット")
                    .addLore("§71個につき成功率+1%"));
        }

        Optional<BaseArtifact> artifactOpt = ItemToArtifact.convert(enhanceTarget);
        double baseProb = 0.0;
        int augmentCount = (augment != null) ? augment.getAmount() : 0;

        if (artifactOpt.isPresent()) {
            int lv = artifactOpt.get().getLv();
            baseProb = 1.0 - ((1.0 - 0.6) / 29.0) * lv;
        }

        double finalProb = baseProb + (augmentCount * 0.01);
        if (finalProb > 1.0) finalProb = 1.0;

        final double displayProb = finalProb;
        setItem(EXECUTE_BUTTON, new ItemBuilder()
                .setMaterial(Material.ANVIL)
                .setName("§a強化を実行する")
                .addLore("§7確率: %s".formatted(String.format("%.2f", displayProb * 100))+"%")
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
                    if (checkLevelCap(baseArtifact)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§cこのアーティファクトはこれ以上強化できません！");
                        return;
                    }
                    boolean isSuccess = Math.random() < displayProb;
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 0.5f);
                    if (isSuccess) {
                        baseArtifact.performEnhance();
                        player.sendMessage("§a強化成功！ Lv" + (baseArtifact.getLv() - 1) + " -> " + baseArtifact.getLv());
                    } else {
                        player.sendMessage("§cアーティファクトの強化に失敗しました...");
                    }
                    ItemStack enhancedItem = ArtifactToItem.convert(baseArtifact);
                    new ArtifactEnhanceMenu(enhancedItem, new ArrayList<>(), null).open(player);
                })
        );
    }

    private boolean checkLevelCap(BaseArtifact artifact){
        int lv = artifact.getLv();
        String tier = artifact.getTier().getText;
        switch (tier){
            case "C" -> {
                return lv >= 10;
            }
            case "B" -> {
                return lv >= 15;
            }
            case "A" -> {
                return lv >= 20;
            }
            case "S" -> {
                return lv >= 25;
            }
            case "SS" -> {
                return lv >= 30;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean checkArtifactSeries(BaseArtifact artifact_1, BaseArtifact artifact_2){
        if (!ArtifactMain.getGeneralConfig().isSeriesBinding()) {
            return true;
        }
        if (artifact_1.getSeries() == null || artifact_2.getSeries() == null) {
            return false;
        }
        return artifact_1.getSeries().equals(artifact_2.getSeries());
    }
}
