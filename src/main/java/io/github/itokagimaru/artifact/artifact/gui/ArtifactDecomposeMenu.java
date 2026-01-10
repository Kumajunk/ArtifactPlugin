package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.decompose.DecomposeConfig;
import io.github.itokagimaru.artifact.artifact.decompose.LootEntry;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * アーティファクト分解GUI
 * アーティファクトを分解してオーグメントと追加報酬を獲得
 */
public class ArtifactDecomposeMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    private static final int ARTIFACT_SLOT = 13;
    private static final int INFO_SLOT = 22;
    private static final int REWARD_PREVIEW_SLOT = 31;
    private static final int EXECUTE_BUTTON = 49;

    private final ItemStack targetArtifact;

    public ArtifactDecomposeMenu() {
        this(null);
    }

    public ArtifactDecomposeMenu(ItemStack targetArtifact) {
        super(GUI_SIZE, "§lアーティファクト分解");
        this.targetArtifact = targetArtifact;
        setupGui();
        setupPlayerInventoryHandler();
    }

    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            // アーティファクトかどうか確認
            if (!ItemToArtifact.isArtifact(item)) {
                player.sendMessage("§cこれはアーティファクトではありません！");
                return;
            }

            if (targetArtifact == null) {
                // アーティファクトをセット
                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                new ArtifactDecomposeMenu(item).open(player);
            } else {
                player.sendMessage("§c既にアーティファクトがセットされています！");
            }
        });
    }

    private void setupGui() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        if (targetArtifact != null) {
            // アーティファクトをセット済み
            setReturnItem(ARTIFACT_SLOT, targetArtifact, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(targetArtifact);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactDecomposeMenu(null).open(player);
            });

            // 報酬情報を表示
            Optional<BaseArtifact> artifactOpt = ItemToArtifact.convert(targetArtifact);
            if (artifactOpt.isPresent()) {
                BaseArtifact artifact = artifactOpt.get();
                int augmentReward = calculateAugmentReward(artifact);
                Series.artifactSeres series = artifact.getSeries();

                // 情報表示
                ItemBuilder infoBuilder = new ItemBuilder()
                        .setMaterial(Material.PAPER)
                        .setName("§e分解報酬情報")
                        .addLore("§7シリーズ: §f" + (series != null ? artifact.getSeriesName() : "不明"))
                        .addLore("§7ティア: §f" + artifact.getTier().getText)
                        .addLore("§7レベル: §f" + artifact.getLv())
                        .addLore("")
                        .addLore("§6オーグメント報酬: §e" + augmentReward + "個");

                // ルートテーブル報酬を表示
                if (series != null) {
                    DecomposeConfig config = ArtifactMain.getDecomposeConfig();
                    List<LootEntry> lootTable = config.getLootTable(series);
                    if (!lootTable.isEmpty()) {
                        infoBuilder.addLore("");
                        infoBuilder.addLore("§d追加報酬:");
                        for (LootEntry entry : lootTable) {
                            String itemName = entry.getItem().getType().name();
                            int percent = (int) (entry.getChance() * 100);
                            infoBuilder.addLore("§7- " + itemName + " §8(" + percent + "%)");
                        }
                    }
                }

                setItem(INFO_SLOT, infoBuilder);

                // オーグメント報酬プレビュー
                ItemStack augmentPreview = SpecialItems.getAugment().clone();
                augmentPreview.setAmount(Math.min(augmentReward, 64));
                setItem(REWARD_PREVIEW_SLOT, augmentPreview, null);
            }

            // 分解実行ボタン
            setItem(EXECUTE_BUTTON, new ItemBuilder()
                    .setMaterial(Material.TNT)
                    .setName("§c分解を実行する")
                    .addLore("§7クリックでアーティファクトを分解します")
                    .addLore("§c※この操作は取り消せません！")
                    .setClickAction(ClickType.LEFT, player -> {
                        if (targetArtifact == null) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            player.sendMessage("§c分解するアーティファクトがセットされていません！");
                            return;
                        }

                        Optional<BaseArtifact> opt = ItemToArtifact.convert(targetArtifact);
                        if (opt.isEmpty()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            player.sendMessage("§cアーティファクトの情報が取得できませんでした！");
                            return;
                        }

                        BaseArtifact art = opt.get();
                        int augmentCount = calculateAugmentReward(art);
                        Series.artifactSeres series = art.getSeries();

                        // オーグメントを付与
                        int remaining = augmentCount;
                        while (remaining > 0) {
                            ItemStack augment = SpecialItems.getAugment();
                            int give = Math.min(remaining, 64);
                            augment.setAmount(give);
                            Map<Integer, ItemStack> leftover = player.getInventory().addItem(augment);
                            if (!leftover.isEmpty()) {
                                leftover.values().forEach(i ->
                                        player.getWorld().dropItemNaturally(player.getLocation(), i)
                                );
                            }
                            remaining -= give;
                        }

                        // 追加報酬を付与
                        if (series != null) {
                            DecomposeConfig config = ArtifactMain.getDecomposeConfig();
                            List<LootEntry> lootTable = config.getLootTable(series);
                            for (LootEntry entry : lootTable) {
                                if (entry.shouldDrop()) {
                                    ItemStack dropItem = entry.getItem().clone();
                                    dropItem.setAmount(entry.getRandomAmount());
                                    Map<Integer, ItemStack> leftover = player.getInventory().addItem(dropItem);
                                    if (!leftover.isEmpty()) {
                                        leftover.values().forEach(i ->
                                                player.getWorld().dropItemNaturally(player.getLocation(), i)
                                        );
                                    }
                                }
                            }
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 1.5f);
                        player.sendMessage("§aアーティファクトを分解しました！");
                        player.sendMessage("§6オーグメントを" + augmentCount + "個獲得しました！");

                        // GUIを閉じる
                        new ArtifactDecomposeMenu(null).open(player);
                    })
            );

        } else {
            // アーティファクト未セット
            setItem(ARTIFACT_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c分解するアーティファクトをセットしてください")
                    .addLore("§7インベントリのアーティファクトをクリック"));

            setItem(INFO_SLOT, new ItemBuilder()
                    .setMaterial(Material.PAPER)
                    .setName("§e分解報酬情報")
                    .addLore("§7アーティファクトをセットすると")
                    .addLore("§7報酬情報が表示されます"));

            setItem(EXECUTE_BUTTON, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§8分解を実行する")
                    .addLore("§7アーティファクトをセットしてください"));
        }
    }

    /**
     * オーグメント報酬数を計算
     * N = round(T × (1 + 0.65 × Level))
     *
     * @param artifact 対象アーティファクト
     * @return オーグメント報酬数
     */
    private int calculateAugmentReward(BaseArtifact artifact) {
        double tierMultiplier = ArtifactMain.getGeneralConfig()
                .getTierMultiplier(artifact.getTier().getText);
        int level = artifact.getLv();
        
        double reward = tierMultiplier * (1 + 0.65 * level);
        return (int) Math.round(reward);
    }
}
