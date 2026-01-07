package io.github.itokagimaru.artifact.auction.data;

import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLiteデータベースの接続管理とテーブル作成を行うクラス
 * シングルトンパターンではなく、プラグインのライフサイクルに合わせて管理。
 */
public class AuctionDatabase {
    
    private final JavaPlugin plugin;
    private final AuctionConfig config;
    private Connection connection;

    /**
     * コンストラクタ
     * 
     * @param plugin プラグインインスタンス
     * @param config オークション設定
     */
    public AuctionDatabase(JavaPlugin plugin, AuctionConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * データベースに接続する
     * 
     * @throws SQLException 接続失敗時
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, config.getDatabaseFilename()).getAbsolutePath();
        connection = DriverManager.getConnection(url);
        
        plugin.getLogger().info("Connected to SQLite database: " + config.getDatabaseFilename());
    }

    /**
     * テーブルを初期化する
     * 
     * @throws SQLException テーブル作成失敗時
     */
    public void initTables() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("データベースに接続されていません");
        }

        try (Statement statement = connection.createStatement()) {
            // 出品テーブル
            statement.execute("""
                CREATE TABLE IF NOT EXISTS auction_listings (
                    listing_id TEXT PRIMARY KEY,
                    seller_id TEXT NOT NULL,
                    artifact_id TEXT NOT NULL UNIQUE,
                    artifact_data TEXT NOT NULL,
                    type TEXT NOT NULL,
                    price INTEGER NOT NULL,
                    current_bid INTEGER DEFAULT 0,
                    current_bidder_id TEXT,
                    created_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    -- 検索用のインデックスカラム（artifact_dataからパース）
                    series_id INTEGER,
                    slot_id INTEGER,
                    level INTEGER,
                    main_effect_id INTEGER,
                    main_effect_value INTEGER,
                    sub_effect_ids TEXT,
                    sub_effect_count INTEGER
                )
            """);

            // インデックス作成
            statement.execute("CREATE INDEX IF NOT EXISTS idx_seller ON auction_listings(seller_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_expires ON auction_listings(expires_at)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_series ON auction_listings(series_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_slot ON auction_listings(slot_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_level ON auction_listings(level)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_price ON auction_listings(price)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_type ON auction_listings(type)");

            // 入札履歴テーブル
            statement.execute("""
                CREATE TABLE IF NOT EXISTS bid_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    listing_id TEXT NOT NULL,
                    bidder_id TEXT NOT NULL,
                    bid_amount INTEGER NOT NULL,
                    bid_time INTEGER NOT NULL,
                    FOREIGN KEY (listing_id) REFERENCES auction_listings(listing_id)
                )
            """);

            statement.execute("CREATE INDEX IF NOT EXISTS idx_bid_listing ON bid_history(listing_id)");

            plugin.getLogger().info("Auction tables initialized");
        }
    }

    /**
     * データベース接続を取得
     * 
     * @return Connection オブジェクト
     * @throws SQLException 接続が閉じている場合
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    /**
     * データベース接続を閉じる
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("SQLite database connection closed");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}
