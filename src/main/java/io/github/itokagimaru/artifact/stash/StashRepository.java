package io.github.itokagimaru.artifact.stash;

import io.github.itokagimaru.artifact.auction.data.AuctionDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * InventoryStashのデータ永続化を担当するリポジトリクラス
 */
public class StashRepository {

    private final JavaPlugin plugin;
    private final AuctionDatabase database;
    private final ExecutorService dbExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public StashRepository(JavaPlugin plugin, AuctionDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void shutdown() {
        dbExecutor.shutdown();
    }

    /**
     * Stashテーブルを初期化する
     */
    public void initTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS inventory_stash (
                id VARCHAR(36) PRIMARY KEY,
                player_id VARCHAR(36) NOT NULL,
                item_data TEXT NOT NULL,
                source VARCHAR(255) NOT NULL,
                created_at BIGINT NOT NULL,
                INDEX idx_stash_player (player_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;

        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * アイテムをStashに保存する
     */
    public void save(StashItem item) throws SQLException {
        String sql = """
            INSERT INTO inventory_stash (id, player_id, item_data, source, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getPlayerId().toString());
            stmt.setString(3, item.getItemData());
            stmt.setString(4, item.getSource());
            stmt.setLong(5, item.getCreatedAt());
            stmt.executeUpdate();
        }
    }

    public CompletableFuture<Void> saveAsync(StashItem item) {
        return CompletableFuture.runAsync(() -> {
            try {
                save(item);
            } catch (SQLException e) {
                plugin.getLogger().severe("Stash保存エラー: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    /**
     * アイテムをStashから削除する
     */
    public void delete(UUID itemId) throws SQLException {
        String sql = "DELETE FROM inventory_stash WHERE id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId.toString());
            stmt.executeUpdate();
        }
    }

    public CompletableFuture<Void> deleteAsync(UUID itemId) {
        return CompletableFuture.runAsync(() -> {
            try {
                delete(itemId);
            } catch (SQLException e) {
                plugin.getLogger().severe("Stash削除エラー: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    /**
     * プレイヤーのStashアイテムを取得する
     */
    public List<StashItem> findByPlayer(UUID playerId) {
        String sql = "SELECT * FROM inventory_stash WHERE player_id = ? ORDER BY created_at DESC";
        List<StashItem> items = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Stash検索エラー: " + e.getMessage());
        }

        return items;
    }

    public CompletableFuture<List<StashItem>> findByPlayerAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findByPlayer(playerId);
            } catch (Exception e) {
                plugin.getLogger().severe("Stash検索エラー: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    /**
     * プレイヤーのStashアイテム数を取得する
     */
    public int countByPlayer(UUID playerId) {
        String sql = "SELECT COUNT(*) FROM inventory_stash WHERE player_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Stashカウントエラー: " + e.getMessage());
        }

        return 0;
    }

    public CompletableFuture<Integer> countByPlayerAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return countByPlayer(playerId);
            } catch (Exception e){
                plugin.getLogger().severe(e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    /**
     * アイテムをIDで取得する
     */
    public StashItem findById(UUID itemId) {
        String sql = "SELECT * FROM inventory_stash WHERE id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Stash検索エラー: " + e.getMessage());
        }

        return null;
    }

    public CompletableFuture<StashItem> findByIdAsync(UUID itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findById(itemId);
            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    private StashItem mapResultSet(ResultSet rs) throws SQLException {
        return new StashItem(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("player_id")),
            rs.getString("item_data"),
            rs.getString("source"),
            rs.getLong("created_at")
        );
    }
}
