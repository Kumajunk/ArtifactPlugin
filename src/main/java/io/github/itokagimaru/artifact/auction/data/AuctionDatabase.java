package io.github.itokagimaru.artifact.auction.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQLデータベースの接続管理とテーブル作成を行うクラス
 * HikariCPを使用してコネクションプールを管理する。
 */
public class AuctionDatabase {

    private final JavaPlugin plugin;
    private final AuctionConfig config;
    private HikariDataSource dataSource;

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
     * HikariCPのコネクションプールを初期化する。
     */
    public void connect() {
        HikariConfig hikariConfig = new HikariConfig();

        // MySQL JDBC URL
        String jdbcUrl = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        // コネクションプール設定
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        // MySQL最適化設定
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        // プールの識別名
        hikariConfig.setPoolName("ArtifactPlugin-HikariPool");

        dataSource = new HikariDataSource(hikariConfig);
        plugin.getLogger().info("MySQL database connection pool initialized");
    }

    /**
     * テーブルを初期化する
     *
     * @throws SQLException テーブル作成失敗時
     */
    public void initTables() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // 出品テーブル（MySQL構文）
            statement.execute("""
                CREATE TABLE IF NOT EXISTS auction_listings (
                    listing_id VARCHAR(36) PRIMARY KEY,
                    seller_id VARCHAR(36) NOT NULL,
                    artifact_id VARCHAR(36) NOT NULL UNIQUE,
                    artifact_data TEXT NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    price BIGINT NOT NULL,
                    current_bid BIGINT DEFAULT 0,
                    current_bidder_id VARCHAR(36),
                    created_at BIGINT NOT NULL,
                    expires_at BIGINT NOT NULL,
                    -- 検索用のインデックスカラム（artifact_dataからパース）
                    series_id INT,
                    slot_id INT,
                    level INT,
                    main_effect_id INT,
                    main_effect_value INT,
                    sub_effect_ids TEXT,
                    sub_effect_count INT,
                    INDEX idx_seller (seller_id),
                    INDEX idx_expires (expires_at),
                    INDEX idx_series (series_id),
                    INDEX idx_slot (slot_id),
                    INDEX idx_level (level),
                    INDEX idx_price (price),
                    INDEX idx_type (type)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);

            // 入札履歴テーブル（MySQL構文）
            statement.execute("""
                CREATE TABLE IF NOT EXISTS bid_history (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    listing_id VARCHAR(36) NOT NULL,
                    bidder_id VARCHAR(36) NOT NULL,
                    bid_amount BIGINT NOT NULL,
                    bid_time BIGINT NOT NULL,
                    INDEX idx_bid_listing (listing_id),
                    FOREIGN KEY (listing_id) REFERENCES auction_listings(listing_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);

            plugin.getLogger().info("Auction tables initialized");
        }
    }

    /**
     * データベース接続を取得
     * コネクションプールから接続を取得する。
     *
     * @return Connection オブジェクト
     * @throws SQLException DataSourceが初期化されていない場合、または接続取得失敗時
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized. Call connect() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * データベース接続を閉じる
     * コネクションプールをシャットダウンする。
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MySQL database connection pool closed");
        }
    }

    /**
     * コネクションプールが有効かどうかを確認する
     *
     * @return プールが有効な場合true
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
