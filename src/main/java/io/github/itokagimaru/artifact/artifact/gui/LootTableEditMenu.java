package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import io.github.itokagimaru.artifact.artifact.decompose.DecomposeConfig;
import io.github.itokagimaru.artifact.artifact.decompose.LootEntry;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * ルートテーブル編集GUI（管理者用）
 * シリーズごとの分解報酬アイテムを設定
 */
public class LootTableEditMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    
    private final Series selectedSeries;
    private ItemStack pendingItem;
    private double pendingChance = 1.0;
    private int pendingMinAmount = 1;
    private int pendingMaxAmount = 1;

    /**
     * シリーズ選択画面のコンストラクタ
     */
    public LootTableEditMenu() {
        super(GUI_SIZE, "§l分解テーブル - シリーズ選択");
        this.selectedSeries = null;
        setupSeriesSelectGui();
    }

    /**
     * エントリ編集画面のコンストラクタ
     *
     * @param series 編集対象シリーズ
     */
    public LootTableEditMenu(Series series) {
        super(GUI_SIZE, "§l分解テーブル - " + series.getSeriesName());
        this.selectedSeries = series;
        setupLootEditGui();
        setupPlayerInventoryHandler();
    }

    /**
     * シリーズ選択画面のセットアップ
     */
    private void setupSeriesSelectGui() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        Series[] seriesList = SeriesRegistry.getAllSeries();
        int startSlot = 10;
        
        for (int i = 0; i < seriesList.length; i++) {
            Series series = seriesList[i];
            int slot = startSlot + (i % 7) + (i / 7) * 9;
            
            DecomposeConfig config = ArtifactMain.getDecomposeConfig();
            int entryCount = config.getLootTable(series).size();

            setItem(slot, new ItemBuilder()
                    .setMaterial(Material.BOOK)
                    .setName("§e" + series.getSeriesName())
                    .addLore("§7登録アイテム数: §f" + entryCount)
                    .addLore("")
                    .addLore("§aクリックで編集")
                    .setClickAction(ClickType.LEFT, player -> {
                        new LootTableEditMenu(series).open(player);
                    })
            );
        }
    }

    /**
     * ルートエントリ編集画面のセットアップ
     */
    private void setupLootEditGui() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // 戻るボタン
        setItem(0, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§c戻る")
                .setClickAction(ClickType.LEFT, player -> {
                    new LootTableEditMenu().open(player);
                })
        );

        // 現在の登録アイテム一覧
        DecomposeConfig config = ArtifactMain.getDecomposeConfig();
        List<LootEntry> entries = config.getLootTable(selectedSeries);

        for (int i = 0; i < Math.min(entries.size(), 21); i++) {
            LootEntry entry = entries.get(i);
            int slot = 10 + (i % 7) + (i / 7) * 9;
            int index = i;

            ItemStack displayItem = entry.getItem().clone();
            
            setItem(slot, new ItemBuilder()
                    .setMaterial(displayItem.getType())
                    .setName("§e" + displayItem.getType().name())
                    .addLore("§7確率: §f" + (int)(entry.getChance() * 100) + "%")
                    .addLore("§7数量: §f" + entry.getMinAmount() + "〜" + entry.getMaxAmount())
                    .addLore("")
                    .addLore("§cシフト+クリックで削除")
                    .setClickAction(ClickType.SHIFT_LEFT, player -> {
                        config.removeLootEntry(selectedSeries, index);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
                        player.sendMessage("§cエントリを削除しました");
                        new LootTableEditMenu(selectedSeries).open(player);
                    })
            );
        }

        // 新規追加スロット
        setItem(40, new ItemBuilder()
                .setMaterial(Material.LIME_STAINED_GLASS_PANE)
                .setName("§aアイテムを追加")
                .addLore("§7インベントリのアイテムをクリックして")
                .addLore("§7ルートテーブルに追加できます")
        );

        // ペンディングアイテムがある場合
        if (pendingItem != null) {
            setItem(49, new ItemBuilder()
                    .setMaterial(Material.EMERALD)
                    .setName("§a追加を確定")
                    .addLore("§7アイテム: §f" + pendingItem.getType().name())
                    .addLore("§7確率: §f" + (int)(pendingChance * 100) + "%")
                    .addLore("§7数量: §f" + pendingMinAmount + "〜" + pendingMaxAmount)
                    .addLore("")
                    .addLore("§eクリックで追加")
                    .setClickAction(ClickType.LEFT, player -> {
                        LootEntry newEntry = new LootEntry(pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount);
                        config.addLootEntry(selectedSeries, newEntry);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        player.sendMessage("§aアイテムをルートテーブルに追加しました");
                        new LootTableEditMenu(selectedSeries).open(player);
                    })
            );

            // 確率調整
            setItem(46, new ItemBuilder()
                    .setMaterial(Material.RED_DYE)
                    .setName("§c確率を下げる (-10%)")
                    .addLore("§7現在: " + (int)(pendingChance * 100) + "%")
                    .setClickAction(ClickType.LEFT, player -> {
                        pendingChance = Math.max(0.1, pendingChance - 0.1);
                        new LootTableEditMenu(selectedSeries, pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount).open(player);
                    })
            );

            setItem(47, new ItemBuilder()
                    .setMaterial(Material.LIME_DYE)
                    .setName("§a確率を上げる (+10%)")
                    .addLore("§7現在: " + (int)(pendingChance * 100) + "%")
                    .setClickAction(ClickType.LEFT, player -> {
                        pendingChance = Math.min(1.0, pendingChance + 0.1);
                        new LootTableEditMenu(selectedSeries, pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount).open(player);
                    })
            );

            // 数量調整
            setItem(51, new ItemBuilder()
                    .setMaterial(Material.RED_DYE)
                    .setName("§c最大数量を減らす")
                    .addLore("§7現在: " + pendingMaxAmount)
                    .setClickAction(ClickType.LEFT, player -> {
                        pendingMaxAmount = Math.max(pendingMinAmount, pendingMaxAmount - 1);
                        new LootTableEditMenu(selectedSeries, pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount).open(player);
                    })
            );

            setItem(52, new ItemBuilder()
                    .setMaterial(Material.LIME_DYE)
                    .setName("§a最大数量を増やす")
                    .addLore("§7現在: " + pendingMaxAmount)
                    .setClickAction(ClickType.LEFT, player -> {
                        pendingMaxAmount++;
                        new LootTableEditMenu(selectedSeries, pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount).open(player);
                    })
            );
        }
    }

    /**
     * ペンディングアイテム付きコンストラクタ（内部用）
     */
    private LootTableEditMenu(Series series, ItemStack pendingItem, 
                               double pendingChance, int pendingMinAmount, int pendingMaxAmount) {
        super(GUI_SIZE, "§l分解テーブル - " + series.getSeriesName());
        this.selectedSeries = series;
        this.pendingItem = pendingItem;
        this.pendingChance = pendingChance;
        this.pendingMinAmount = pendingMinAmount;
        this.pendingMaxAmount = pendingMaxAmount;
        setupLootEditGui();
        setupPlayerInventoryHandler();
    }

    /**
     * プレイヤーインベントリクリックハンドラ
     */
    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            if (selectedSeries == null) {
                player.sendMessage("§c先にシリーズを選択してください");
                return;
            }

            // アイテムをペンディングにセット（消費しない）
            this.pendingItem = item.clone();
            this.pendingItem.setAmount(1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.sendMessage("§aアイテムを選択しました: " + item.getType().name());
            new LootTableEditMenu(selectedSeries, pendingItem, pendingChance, pendingMinAmount, pendingMaxAmount).open(player);
        });
    }
}
