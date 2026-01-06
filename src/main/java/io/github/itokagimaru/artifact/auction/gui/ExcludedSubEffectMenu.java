package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;

public class ExcludedSubEffectMenu extends BaseGui {
    private final AuctionManager manager;
    private final AuctionSearchFilter filter;
    private SortOrder sortOrder;

    public ExcludedSubEffectMenu(AuctionManager manager, AuctionSearchFilter filter, SortOrder sortOrder) {
        super(54, "§6不必要なSub効果を選択");
        this.manager = manager;
        this.filter = filter;
        this.sortOrder = sortOrder;
        setupMenu();
    }

    private void setupMenu() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        int slot = 18;

        for (SubEffect.artifactSubEffect effect : SubEffect.artifactSubEffect.values()) {
            boolean isSelected = filter.getExcludedSubEffects().contains(effect);
            boolean isRequired = filter.getRequiredSubEffects().contains(effect);
            Material mainMaterial = Material.WOODEN_SHOVEL;
            switch(slot) {
                case 18 -> mainMaterial = Material.APPLE;
                case 19 -> mainMaterial = Material.IRON_SWORD;
                case 20 -> mainMaterial = Material.IRON_CHESTPLATE;
                case 21 -> mainMaterial = Material.GOLDEN_APPLE;
                case 22 -> mainMaterial = Material.ECHO_SHARD;
                case 23 -> mainMaterial = Material.END_CRYSTAL;
                case 24 -> mainMaterial = Material.LAVA_BUCKET;
                case 25 -> mainMaterial = Material.WATER_BUCKET;
                case 26 -> mainMaterial = Material.MOSS_BLOCK;
            }
            Material subMaterial = isSelected ? Material.LIME_DYE : Material.GRAY_DYE;

            setItem(slot, new ItemBuilder()
                    .setMaterial(mainMaterial)
                    .setName("§a" + effect.name() + " Effect")
            );

            setItem(slot+9, new ItemBuilder()
                    .setMaterial(subMaterial)
                    .setName("§7クリックして選択/解除")
                    .setClickAction(ClickType.LEFT, player -> {
                        if (isRequired) {
                            player.sendMessage("§c§lこのSub効果は必須効果として設定されています!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        if (isSelected) {
                            // すでに選ばれているなら削除
                            filter.getExcludedSubEffects().remove(effect);
                        } else {
                            // 選ばれていないなら追加
                            filter.getExcludedSubEffects().add(effect);
                        }
                        // 再描画
                        new ExcludedSubEffectMenu(manager, filter, sortOrder).open(player);
                    })
            );
            slot++;
        }

        setItem(49, new ItemBuilder()
                .setMaterial(Material.BARRIER)
                .setName("§c§l戻る")
                .setClickAction(ClickType.LEFT, player -> {
                    new AuctionSearchMenu(manager, filter, sortOrder).open(player);
                })
        );
    }
}
