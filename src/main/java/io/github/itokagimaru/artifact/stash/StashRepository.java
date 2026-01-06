package io.github.itokagimaru.artifact.stash;

import io.github.itokagimaru.artifact.auction.data.AuctionDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * InventoryStashのデータ永続化を担当するリポジトリクラス
 */
public class StashRepository {

    private final JavaPlugin plugin;
    private final AuctionDatabase database;

    public StashRepository(JavaPlugin plugin, AuctionDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Stashテーブルを初期化する
     */
    public void initTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS inventory_stash (
                id TEXT PRIMARY KEY,
                player_id TEXT NOT NULL,
                item_data TEXT NOT NULL,
                source TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """;

        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        // インデックス作成
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_stash_player ON inventory_stash(player_id)";
        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(indexSql);
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
