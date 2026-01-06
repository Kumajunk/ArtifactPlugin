package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;

public class RequiredSubEffectMenu extends BaseGui {

    private final AuctionManager manager;
    private final AuctionSearchFilter filter;
    private SortOrder sortOrder;

    public RequiredSubEffectMenu(AuctionManager manager, AuctionSearchFilter filter, SortOrder sortOrder) {
        super(54, "§6必須のSub効果を選択");
        this.manager = manager;
        this.filter = filter;
        this.sortOrder = sortOrder;
        setupMenu();
    }

    private void setupMenu() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        int slot = 18;

        for (SubEffect.artifactSubEffect effect : SubEffect.artifactSubEffect.values()) {
            boolean isSelected = filter.getRequiredSubEffects().contains(effect);
            boolean isExcluded = filter.getExcludedSubEffects().contains(effect);
            Material mainMaterial = Material.IRON_SWORD;
            switch(slot) {
                case 18 -> mainMaterial = Material.APPLE;
                case 19 -> mainMaterial = Material.BREAD;
                case 20 -> mainMaterial = Material.CARROT;
                case 21 -> mainMaterial = Material.POTATO;
                case 22 -> mainMaterial = Material.BEETROOT;
                case 23 -> mainMaterial = Material.MELON;
                case 24 -> mainMaterial = Material.PUMPKIN;
                case 25 -> mainMaterial = Material.SWEET_BERRIES;
                case 26 -> mainMaterial = Material.GLOW_BERRIES;
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
                        if (isExcluded) {
                            // 除外リストにある場合は何もしない
                            player.sendMessage("§cその効果は除外リストに登録されています。先に除外リストから削除してください。");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        if (isSelected) {
                            // すでに選ばれているなら削除
                            filter.getRequiredSubEffects().remove(effect);
                        } else {
                            // 選ばれていないなら追加
                            filter.getRequiredSubEffects().add(effect);
                        }
                        // 再描画
                        new RequiredSubEffectMenu(manager, filter, sortOrder).open(player);
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
