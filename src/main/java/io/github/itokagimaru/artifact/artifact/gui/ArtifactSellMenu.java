package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

import static java.lang.Math.pow;

public class ArtifactSellMenu extends BaseGui {
    private BaseArtifact sellArtifact;
    private int augmentCount;

    public ArtifactSellMenu() {
        this(null, 0);
    }

    public ArtifactSellMenu(BaseArtifact sellArtifact, int augmentCount) {
        super(54, "Artifact Sell Menu");
        this.sellArtifact = sellArtifact;
        this.augmentCount = augmentCount;
        setupGui();
        setupPlayerInventoryHandler();
    }

    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            if (sellArtifact != null || augmentCount > 0) {
                player.sendMessage("§c既にアーティファクトまたはオーグメントが選択されています。");
                return;
            }
            if (SpecialItems.isAugment(item)) {
                player.sendMessage("§aオーグメントを選択しました。");
                augmentCount = item.getAmount();
                item.setAmount(0);
                new ArtifactSellMenu(sellArtifact, augmentCount).open(player);
                return;
            }
            Optional<BaseArtifact> BaseArtifact = ItemToArtifact.convert(item);
            if (BaseArtifact.isEmpty()) {
                player.sendMessage("§cこのアイテムはアーティファクトではありません");
                return;
            }
            BaseArtifact artifact = BaseArtifact.get();
            item.setAmount(0);
            player.sendMessage("§aアーティファクトを選択しました。");
            sellArtifact = artifact;
            new ArtifactSellMenu(artifact, augmentCount).open(player);
            return;
        });
    }

    private void setupGui() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        if (sellArtifact != null) {
            setReturnItem(13, ArtifactToItem.convert(sellArtifact), player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(ArtifactToItem.convert(sellArtifact));
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactSellMenu(null, 0).open(player);
            });
        } else if (augmentCount > 0) {
            ItemStack augmentItem = SpecialItems.getAugment();
            augmentItem.setAmount(augmentCount);
            setReturnItem(13, augmentItem, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(augmentItem);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactSellMenu(null, 0).open(player);
            });
        } else {
            setItem(13, new ItemBuilder().setMaterial(Material.BARRIER).setName("§cアーティファクトが選択されていません"));
        }

        if (sellArtifact != null) {
            setItem(40, new ItemBuilder()
                    .setMaterial(Material.LIME_WOOL)
                    .setName("§aアーティファクトを売却する")
                    .setClickAction(ClickType.LEFT, player -> {
                        // 100はArtifactのBaseValue(config実装後に変更予定)
                        long sellPrice = Math.round(100.0 * ArtifactMain.getGeneralConfig().getTierMultiplier(sellArtifact.getTier().getText) * (1.0 + ArtifactMain.getGeneralConfig().getLevelScalingRate() * pow(sellArtifact.getLv(), 2)));
                        player.sendMessage("§aアーティファクトを売却しました。売却価格: §6" + sellPrice + " ゴールド");
                        ArtifactMain.getVaultAPI().deposit(player.getUniqueId(), sellPrice);
                        new ArtifactSellMenu(null, 0).open(player);
                    })
            );
        } else if (augmentCount > 0) {
            setItem(40, new ItemBuilder()
                    .setMaterial(Material.LIME_WOOL)
                    .setName("§aオーグメントを売却する")
                    .setClickAction(ClickType.LEFT, player -> {
                        double augmentPrice = ArtifactMain.getGeneralConfig().getAugmentPrice();
                        long totalPrice = Math.round(augmentPrice * augmentCount);
                        player.sendMessage("§aオーグメントを売却しました。売却価格: §6" + totalPrice + " ゴールド");
                        ArtifactMain.getVaultAPI().deposit(player.getUniqueId(), totalPrice);
                        new ArtifactSellMenu(null, 0).open(player);
                    })
            );
        }
    }
}
