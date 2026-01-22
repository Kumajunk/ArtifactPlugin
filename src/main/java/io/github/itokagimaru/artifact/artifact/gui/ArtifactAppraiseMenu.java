package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.itokagimaru.artifact.artifact.items.SpecialItems.isUnidentifiedArtifact;

/**
 * アーティファクト鑑定メニュー
 * 未鑑定アーティファクトを鑑定してティアを決定する
 */
public class ArtifactAppraiseMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    private static final int ARTIFACT_SLOT = 13;
    private static final int CONFIRM_SLOT = 40;

    private ItemStack unidentifiedArtifact;
    private String artifactSeriesId;

    public ArtifactAppraiseMenu() {
        this(null, null);
    }

    public ArtifactAppraiseMenu(ItemStack unidentifiedArtifact, String artifactSeriesId) {
        super(GUI_SIZE, "§lアーティファクト鑑定");
        this.unidentifiedArtifact = unidentifiedArtifact;
        this.artifactSeriesId = artifactSeriesId;
        setupGui();
        setupPlayerInventoryClickHandler();
    }

    /**
     * プレイヤーインベントリクリック時の処理を設定
     */
    private void setupPlayerInventoryClickHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            // 未鑑定アーティファクトかチェック
            if (isUnidentifiedArtifact(item)) {
                // シリーズIDを取得
                String seriesId = item.getItemMeta().getPersistentDataContainer()
                        .get(SpecialItems.UNIDENTIFIED_ARTIFACT_KEY, PersistentDataType.STRING);

                // アイテムをクローンしてからインベントリから消す
                ItemStack clonedItem = item.clone();
                clonedItem.setAmount(1);
                item.setAmount(0);
                player.sendMessage("§a未鑑定のアーティファクトをセットしました");
                new ArtifactAppraiseMenu(clonedItem, seriesId).open(player);
                return;
            }

            player.sendMessage("§c未鑑定のアーティファクトをセットしてください");
        });
    }

    /**
     * GUI要素を設定
     */
    private void setupGui() {
        // 背景を埋める
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // アーティファクトスロット
        if (unidentifiedArtifact == null) {
            setItem(ARTIFACT_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c未鑑定のアーティファクトをセットしてください")
                    .addLore("§7インベントリからクリックしてセット"));
        } else {
            // セット済みの場合、クリックで返却
            ItemStack displayItem = unidentifiedArtifact.clone();
            displayItem.setAmount(1);
            setReturnItem(ARTIFACT_SLOT, displayItem, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(unidentifiedArtifact);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                player.sendMessage("§a未鑑定のアーティファクトを返却しました");
                new ArtifactAppraiseMenu().open(player);
            });
        }

        // 鑑定確定ボタン
        if (unidentifiedArtifact != null) {
            setItem(CONFIRM_SLOT, new ItemBuilder()
                    .setMaterial(Material.EMERALD_BLOCK)
                    .setName("§a§l鑑定を実行する")
                    .addLore("§7クリックでティアを決定します")
                    .addLore("")
                    .addLore("§e ティア確率:")
                    .addLore("§7 SS: §61.0%")
                    .addLore("§7 S: §e2.5%")
                    .addLore("§7 A: §a9.0%")
                    .addLore("§7 B: §b22.5%")
                    .addLore("§7 C: §765.0%")
                    .setClickAction(ClickType.LEFT, player -> {
                        if (unidentifiedArtifact == null || artifactSeriesId == null) {
                            player.sendMessage("§c未鑑定のアーティファクトがセットされていません！");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }

                        // ティアを抽選
                        Tier.artifactTier tier = lotteryTier();

                        // 鑑定済みアーティファクトを生成
                        ItemStack appraisedArtifact = SpecialItems.getAppraisedArtifact(
                                artifactSeriesId,
                                tier.getText
                        );

                        // プレイヤーに渡す
                        Map<Integer, ItemStack> remaining = player.getInventory().addItem(appraisedArtifact);
                        if (!remaining.isEmpty()) {
                            remaining.values().forEach(i ->
                                    player.getWorld().dropItemNaturally(player.getLocation(), i)
                            );
                        }

                        // エフェクト
                        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                        player.sendMessage("§a鑑定完了！ ティア: §e" + tier.getText);

                        // メニューを閉じる
                        player.closeInventory();
                    })
            );
        } else {
            setItem(CONFIRM_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c鑑定できません")
                    .addLore("§7未鑑定のアーティファクトを先にセットしてください"));
        }
    }

    /**
     * ティア抽選
     * @return 抽選されたティア
     */
    public Tier.artifactTier lotteryTier() {
        double roll = ThreadLocalRandom.current().nextDouble(0.0, 100.0);

        if (roll < 1.0) {
            return Tier.artifactTier.SS;
        } else if (roll < 3.5) {
            return Tier.artifactTier.S;
        } else if (roll < 12.5) {
            return Tier.artifactTier.A;
        } else if (roll < 35.0) {
            return Tier.artifactTier.B;
        } else {
            return Tier.artifactTier.C;
        }
    }
}
