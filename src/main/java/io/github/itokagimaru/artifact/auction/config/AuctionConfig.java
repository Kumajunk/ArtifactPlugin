package io.github.itokagimaru.artifact.auction.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * オークション設定を管理するクラス
 * auction_config.yml から設定を読み込み、
 * 各種設定値を提供する。
 */
public class AuctionConfig {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    // キャッシュされた設定値
    private int maxListingsPerPlayer;
    private int defaultDurationHours;
    private int maxDurationHours;
    private int cancelWindowHours;
    private double listingFee;
    private double saleFee;
    private long minBidIncrement;
    private boolean lockBidAmount;
    private String databaseFilename;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private List<String> allowedWorlds;

    /**
     * コンストラクタ
     * 
     * @param plugin プラグインインスタンス
     */
    public AuctionConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 設定ファイルを読み込む
     */
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "auction_config.yml");
        
        // ファイルが存在しない場合はデフォルトを作成
        if (!configFile.exists()) {
            plugin.saveResource("auction_config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // デフォルト値をマージ
        InputStream defaultStream = plugin.getResource("auction_config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
        
        // 値をキャッシュ
        cacheValues();
    }

    /**
     * 設定値をキャッシュする
     */
    private void cacheValues() {
        maxListingsPerPlayer = config.getInt("listing.max-per-player", 10);
        defaultDurationHours = config.getInt("listing.default-duration", 48);
        maxDurationHours = config.getInt("listing.max-duration", 168);
        cancelWindowHours = config.getInt("listing.cancel-window", 24);
        listingFee = config.getDouble("fees.listing-fee", 0.05);
        saleFee = config.getDouble("fees.sale-fee", 0.10);
        minBidIncrement = config.getLong("auction.min-bid-increment", 100);
        lockBidAmount = config.getBoolean("auction.lock-bid-amount", true);
        databaseFilename = config.getString("database.filename", "auction.db");
        // databaseセクションから値を読み込む
        host = config.getString("database.host", "localhost");
        port = config.getInt("database.port", 3306);
        database = config.getString("database.database", "artifact_auction");
        username = config.getString("database.username", "root");
        password = config.getString("database.password", "");
        allowedWorlds = config.getStringList("auction.allowed-worlds");
        if (allowedWorlds.isEmpty()) {
            allowedWorlds.add("world");
        }
    }

    /**
     * 設定をリロードする
     */
    public void reload() {
        loadConfig();
    }

    /**
     * 設定を保存する
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save auction config: " + e.getMessage());
        }
    }

    // ========== Getter ==========

    /**
     * プレイヤーごとの最大出品数を取得
     */
    public int getMaxListingsPerPlayer() {
        return maxListingsPerPlayer;
    }

    /**
     * デフォルト出品期間を取得（時間）
     */
    public int getDefaultDurationHours() {
        return defaultDurationHours;
    }

    /**
     * デフォルト出品期間を取得（ミリ秒）
     */
    public long getDefaultDurationMillis() {
        return (long) defaultDurationHours * 60 * 60 * 1000;
    }

    /**
     * 最大出品期間を取得（時間）
     */
    public int getMaxDurationHours() {
        return maxDurationHours;
    }

    /**
     * 最大出品期間を取得（ミリ秒）
     */
    public long getMaxDurationMillis() {
        return (long) maxDurationHours * 60 * 60 * 1000;
    }

    /**
     * キャンセル可能期間を取得（時間）
     */
    public int getCancelWindowHours() {
        return cancelWindowHours;
    }

    /**
     * キャンセル可能期間を取得（ミリ秒）
     */
    public long getCancelWindowMillis() {
        return (long) cancelWindowHours * 60 * 60 * 1000;
    }

    /**
     * 出品時手数料率を取得
     */
    public double getListingFee() {
        return listingFee;
    }

    /**
     * 成立時手数料率を取得
     */
    public double getSaleFee() {
        return saleFee;
    }

    /**
     * 最小入札増加額を取得
     */
    public long getMinBidIncrement() {
        return minBidIncrement;
    }

    /**
     * 入札額をロックするかどうか
     */
    public boolean isLockBidAmount() {
        return lockBidAmount;
    }

    /**
     * データベースファイル名を取得
     */
    public String getDatabaseFilename() {
        return databaseFilename;
    }

    /**
     * 出品手数料を計算
     * 
     * @param price 出品価格
     * @return 手数料額
     */
    public long calculateListingFee(long price) {
        return Math.round(price * listingFee);
    }

    /**
     * 成立手数料を計算
     * 
     * @param price 成立価格
     * @return 手数料額
     */
    public long calculateSaleFee(long price) {
        return Math.round(price * saleFee);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getAllowedWorlds() {
        return allowedWorlds;
    }
}
