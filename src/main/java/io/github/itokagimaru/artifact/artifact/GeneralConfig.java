package io.github.itokagimaru.artifact.artifact;

import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneralConfig {

    private final JavaPlugin plugin;
    private YamlConfiguration config;

    private boolean isTierScaling;
    private List<TierRecord> tierScalingTiers;
    private boolean isLevelScaling;
    private double levelScalingRate;
    private double augmentPrice;

    private boolean isSeriesBinding;

    /**
     * コンストラクタ
     *
     * @param plugin プラグインインスタンス
     */
    public GeneralConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 設定ファイルを読み込む
     */
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "general_config.yml");

        // ファイルが存在しない場合はデフォルトを作成
        if (!configFile.exists()) {
            plugin.saveResource("general_config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // デフォルト値をマージ
        InputStream defaultStream = plugin.getResource("general_config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }

        // 値をキャッシュ
        cacheValues();
    }

    private void cacheValues() {
        isTierScaling = config.getBoolean("sell.tier_scaling.enabled", true);
        List<TierRecord> tiers = new ArrayList<>();
        List<Map<?, ?>> tierMapList = config.getMapList("sell.tier_scaling.tiers");
        for (Map<?, ?> map : tierMapList) {
            String name = (String) map.get("tier");
            double multiplier = ((Number) map.get("multiplier")).doubleValue();
            tiers.add(new TierRecord(name, multiplier));
        }
        tierScalingTiers = tiers;
        isLevelScaling = config.getBoolean("sell.level_scaling.enabled", true);
        levelScalingRate = config.getDouble("sell.level_scaling.rate", 0.5544);

        isSeriesBinding = config.getBoolean("enhance.series_binding.enabled", true);
        augmentPrice = config.getDouble("sell.augment.price", 100.0);
        Tier.artifactTier.reloadTierPriceScale(this);
    }

    public void reload() {
        loadConfig();;
    }

    public boolean isTierScaling() {
        return isTierScaling;
    }

    public double getTierMultiplier(String tierName) {
        return tierScalingTiers.stream()
                .filter(t -> t.tier().equalsIgnoreCase(tierName))
                .findFirst()
                .map(TierRecord::multiplier)
                .orElse(1.0);
    }

    public boolean isLevelScaling() {
        return isLevelScaling;
    }

    public double getLevelScalingRate() {
        return levelScalingRate;
    }

    public double getAugmentPrice() {
        return augmentPrice;
    }

    public boolean isSeriesBinding() {
        return isSeriesBinding;
    }

    public record TierRecord(String tier, double multiplier) {}
}
