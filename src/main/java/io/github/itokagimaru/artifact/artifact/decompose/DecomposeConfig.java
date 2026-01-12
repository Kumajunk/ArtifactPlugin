package io.github.itokagimaru.artifact.artifact.decompose;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * シリーズごとの分解ルートテーブルを管理する設定クラス
 */
public class DecomposeConfig {

    private final JavaPlugin plugin;
    private final File configFile;
    private YamlConfiguration config;

    // シリーズごとのルートテーブル
    private final Map<Series, List<LootEntry>> lootTables = new HashMap<>();

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public DecomposeConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "decompose.yml");
        loadConfig();
    }

    /**
     * 設定ファイルを読み込む
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            // デフォルト設定ファイルを作成
            createDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        parseConfig();
    }

    /**
     * デフォルト設定ファイルを作成
     */
    private void createDefaultConfig() {
        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            
            YamlConfiguration defaultConfig = new YamlConfiguration();
            
            // 各シリーズの空のセクションを作成
            for (Series series : SeriesRegistry.getAllSeries()) {
                defaultConfig.createSection("series." + series.getSeriesName() + ".loot");
            }
            
            defaultConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create decompose.yml: " + e.getMessage());
        }
    }

    /**
     * 設定を解析してルートテーブルを構築
     */
    private void parseConfig() {
        lootTables.clear();

        ConfigurationSection seriesSection = config.getConfigurationSection("series");
        if (seriesSection == null) return;

        for (String seriesName : seriesSection.getKeys(false)) {
            Series series;
            try {
                series = SeriesRegistry.getSeries(seriesName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown series in decompose.yml: " + seriesName);
                continue;
            }

            List<LootEntry> entries = new ArrayList<>();
            List<Map<?, ?>> lootList = config.getMapList("series." + seriesName + ".loot");

            for (Map<?, ?> entryMap : lootList) {
                ItemStack item = (ItemStack) entryMap.get("item");
                if (item == null) continue;

                double chance = entryMap.containsKey("chance") 
                    ? ((Number) entryMap.get("chance")).doubleValue() 
                    : 1.0;
                int minAmount = entryMap.containsKey("minAmount") 
                    ? ((Number) entryMap.get("minAmount")).intValue() 
                    : 1;
                int maxAmount = entryMap.containsKey("maxAmount") 
                    ? ((Number) entryMap.get("maxAmount")).intValue() 
                    : 1;

                entries.add(new LootEntry(item, chance, minAmount, maxAmount));
            }

            lootTables.put(series, entries);
        }
    }

    /**
     * 設定を保存
     */
    public void saveConfig() {
        try {
            // ルートテーブルを設定に反映
            for (Map.Entry<Series, List<LootEntry>> entry : lootTables.entrySet()) {
                String path = "series." + entry.getKey().getSeriesName() + ".loot";
                List<Map<String, Object>> lootList = new ArrayList<>();

                for (LootEntry lootEntry : entry.getValue()) {
                    Map<String, Object> entryMap = new LinkedHashMap<>();
                    entryMap.put("item", lootEntry.getItem());
                    entryMap.put("chance", lootEntry.getChance());
                    entryMap.put("minAmount", lootEntry.getMinAmount());
                    entryMap.put("maxAmount", lootEntry.getMaxAmount());
                    lootList.add(entryMap);
                }

                config.set(path, lootList);
            }

            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save decompose.yml: " + e.getMessage());
        }
    }

    /**
     * 指定シリーズのルートテーブルを取得
     *
     * @param series シリーズ
     * @return ルートエントリのリスト
     */
    public List<LootEntry> getLootTable(Series series) {
        return lootTables.getOrDefault(series, new ArrayList<>());
    }

    /**
     * ルートテーブルにエントリを追加
     *
     * @param series シリーズ
     * @param entry  追加するエントリ
     */
    public void addLootEntry(Series series, LootEntry entry) {
        lootTables.computeIfAbsent(series, k -> new ArrayList<>()).add(entry);
        saveConfig();
    }

    /**
     * ルートテーブルからエントリを削除
     *
     * @param series シリーズ
     * @param index  削除するエントリのインデックス
     */
    public void removeLootEntry(Series series, int index) {
        List<LootEntry> entries = lootTables.get(series);
        if (entries != null && index >= 0 && index < entries.size()) {
            entries.remove(index);
            saveConfig();
        }
    }

    /**
     * 設定をリロード
     */
    public void reload() {
        loadConfig();
    }
}
