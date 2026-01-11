package io.github.itokagimaru.artifact.auction.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * オークション出品データのCRUD操作と検索を行うリポジトリクラス
 */
public class AuctionRepository {

    private final JavaPlugin plugin;
    private final AuctionDatabase database;
    private final Gson gson = new Gson();

    /**
     * コンストラクタ
     *
     * @param plugin   プラグインインスタンス
     * @param database データベースインスタンス
     */
    public AuctionRepository(JavaPlugin plugin, AuctionDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * 出品を保存する
     *
     * @param listing 出品情報
     * @throws SQLException DB操作失敗時
     */
    public void save(AuctionListing listing) throws SQLException {
        // artifact_dataからインデックス用データを抽出
        JsonObject artifactJson = gson.fromJson(listing.getArtifactData(), JsonObject.class);
        int seriesId = artifactJson.has("seriesId") ? artifactJson.get("seriesId").getAsInt() : -1;
        int slotId = artifactJson.has("slotId") ? artifactJson.get("slotId").getAsInt() : -1;
        int level = artifactJson.has("level") ? artifactJson.get("level").getAsInt() : 0;
        int mainEffectId = artifactJson.has("mainEffectId") ? artifactJson.get("mainEffectId").getAsInt() : -1;
        int mainEffectValue = artifactJson.has("mainEffectValue") ? artifactJson.get("mainEffectValue").getAsInt() : 0;
        String subEffectIds = artifactJson.has("subEffectIds") ? artifactJson.get("subEffectIds").getAsString() : "";
        int subEffectCount = artifactJson.has("subEffectCount") ? artifactJson.get("subEffectCount").getAsInt() : 0;

        String sql = """
            INSERT INTO auction_listings
            (listing_id, seller_id, artifact_id, artifact_data, type, price,
             current_bid, current_bidder_id, created_at, expires_at,
             series_id, slot_id, level, main_effect_id, main_effect_value, sub_effect_ids, sub_effect_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                seller_id = VALUES(seller_id),
                artifact_data = VALUES(artifact_data),
                type = VALUES(type),
                price = VALUES(price),
                current_bid = VALUES(current_bid),
                current_bidder_id = VALUES(current_bidder_id),
                expires_at = VALUES(expires_at),
                series_id = VALUES(series_id),
                slot_id = VALUES(slot_id),
                level = VALUES(level),
                main_effect_id = VALUES(main_effect_id),
                main_effect_value = VALUES(main_effect_value),
                sub_effect_ids = VALUES(sub_effect_ids),
                sub_effect_count = VALUES(sub_effect_count)
        """;

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, listing.getListingId().toString());
            stmt.setString(2, listing.getSellerId().toString());
            stmt.setString(3, listing.getArtifactId().toString());
            stmt.setString(4, listing.getArtifactData());
            stmt.setString(5, listing.getType().name());
            stmt.setLong(6, listing.getPrice());
            stmt.setLong(7, listing.getCurrentBid());
            stmt.setString(8, listing.getCurrentBidderId() != null ? listing.getCurrentBidderId().toString() : null);
            stmt.setLong(9, listing.getCreatedAt());
            stmt.setLong(10, listing.getExpiresAt());
            stmt.setInt(11, seriesId);
            stmt.setInt(12, slotId);
            stmt.setInt(13, level);
            stmt.setInt(14, mainEffectId);
            stmt.setInt(15, mainEffectValue);
            stmt.setString(16, subEffectIds);
            stmt.setInt(17, subEffectCount);

            stmt.executeUpdate();
        }
    }

    /**
     * IDで出品を検索する
     *
     * @param listingId 出品ID
     * @return 出品情報（存在しない場合はempty）
     */
    public Optional<AuctionListing> findById(UUID listingId) {
        String sql = "SELECT * FROM auction_listings WHERE listing_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, listingId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToListing(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Listing search error: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * 出品者で検索する
     *
     * @param sellerId 出品者UUID
     * @return 出品リスト
     */
    public List<AuctionListing> findBySeller(UUID sellerId) {
        String sql = "SELECT * FROM auction_listings WHERE seller_id = ? ORDER BY created_at DESC";
        List<AuctionListing> results = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToListing(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Seller listings search error: " + e.getMessage());
        }

        return results;
    }

    /**
     * アーティファクトUUIDで検索する（多重出品チェック用）
     *
     * @param artifactId アーティファクトUUID
     * @return 出品情報（存在しない場合はempty）
     */
    public Optional<AuctionListing> findByArtifactId(UUID artifactId) {
        String sql = "SELECT * FROM auction_listings WHERE artifact_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, artifactId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToListing(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Artifact search error: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * 期限切れの出品を取得する
     *
     * @return 期限切れ出品リスト
     */
    public List<AuctionListing> findExpired() {
        String sql = "SELECT * FROM auction_listings WHERE expires_at <= ?";
        List<AuctionListing> results = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToListing(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Expired listings search error: " + e.getMessage());
        }

        return results;
    }

    /**
     * 条件検索を行う
     *
     * @param filter    検索フィルター
     * @param sortOrder ソート順
     * @param limit     最大取得数
     * @param offset    オフセット
     * @return 出品リスト
     */
    public List<AuctionListing> search(AuctionSearchFilter filter, SortOrder sortOrder, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM auction_listings WHERE expires_at > ?");
        List<Object> params = new ArrayList<>();
        params.add(System.currentTimeMillis());

        // シリーズ条件
        if (!filter.getSeries().isEmpty()) {
            String placeholders = filter.getSeries().stream()
                    .map(s -> "?")
                    .collect(Collectors.joining(","));
            sql.append(" AND series_id IN (").append(placeholders).append(")");
            filter.getSeries().forEach(s -> params.add(s.getSeriesName()));
        }

        // スロット条件
        if (filter.getSlot() != null) {
            sql.append(" AND slot_id = ?");
            params.add(filter.getSlot().getId);
        }

        // レベル範囲
        if (filter.getLevelMin() != null) {
            sql.append(" AND level >= ?");
            params.add(filter.getLevelMin());
        }
        if (filter.getLevelMax() != null) {
            sql.append(" AND level <= ?");
            params.add(filter.getLevelMax());
        }

        // Main効果
        if (filter.getMainEffect() != null) {
            sql.append(" AND main_effect_id = ?");
            params.add(filter.getMainEffect().getId);
        }

        // Main効果数値範囲
        if (filter.getMainEffectValueMin() != null) {
            sql.append(" AND main_effect_value >= ?");
            params.add(filter.getMainEffectValueMin());
        }
        if (filter.getMainEffectValueMax() != null) {
            sql.append(" AND main_effect_value <= ?");
            params.add(filter.getMainEffectValueMax());
        }

        // Sub効果数
        if (filter.getSubEffectCount() != null) {
            sql.append(" AND sub_effect_count = ?");
            params.add(filter.getSubEffectCount());
        }

        // 含まれるべきSub効果（部分一致検索）
        for (SubEffect.artifactSubEffect subEffect : filter.getRequiredSubEffects()) {
            sql.append(" AND sub_effect_ids LIKE ?");
            params.add("%" + subEffect.getId + "%");
        }

        // 除外するSub効果
        for (SubEffect.artifactSubEffect subEffect : filter.getExcludedSubEffects()) {
            sql.append(" AND sub_effect_ids NOT LIKE ?");
            params.add("%" + subEffect.getId + "%");
        }

        // 価格範囲
        if (filter.getPriceMin() != null) {
            sql.append(" AND price >= ?");
            params.add(filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            sql.append(" AND price <= ?");
            params.add(filter.getPriceMax());
        }

        // オークション種別
        if (filter.getAuctionType() != null) {
            sql.append(" AND type = ?");
            params.add(filter.getAuctionType().name());
        }

        // ソート
        sql.append(" ORDER BY ").append(sortOrder.toSqlOrderBy());

        // ページネーション
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<AuctionListing> results = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToListing(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Search error: " + e.getMessage());
        }

        return results;
    }

    /**
     * 出品を削除する
     *
     * @param listingId 出品ID
     * @throws SQLException DB操作失敗時
     */
    public void delete(UUID listingId) throws SQLException {
        String sql = "DELETE FROM auction_listings WHERE listing_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, listingId.toString());
            stmt.executeUpdate();
        }

        // 入札履歴も削除
        String bidSql = "DELETE FROM bid_history WHERE listing_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(bidSql)) {
            stmt.setString(1, listingId.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * 出品者の出品数を取得する
     *
     * @param sellerId 出品者UUID
     * @return 出品数
     */
    public int countBySeller(UUID sellerId) {
        String sql = "SELECT COUNT(*) FROM auction_listings WHERE seller_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Listing count error: " + e.getMessage());
        }

        return 0;
    }

    /**
     * 入札を記録する
     *
     * @param listingId 出品ID
     * @param bidderId  入札者UUID
     * @param bidAmount 入札額
     */
    public void recordBid(UUID listingId, UUID bidderId, long bidAmount) throws SQLException {
        String sql = "INSERT INTO bid_history (listing_id, bidder_id, bid_amount, bid_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, listingId.toString());
            stmt.setString(2, bidderId.toString());
            stmt.setLong(3, bidAmount);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }

    /**
     * ResultSetからAuctionListingにマッピングする
     */
    private AuctionListing mapResultSetToListing(ResultSet rs) throws SQLException {
        String currentBidderIdStr = rs.getString("current_bidder_id");
        UUID currentBidderId = currentBidderIdStr != null ? UUID.fromString(currentBidderIdStr) : null;

        return new AuctionListing(
                UUID.fromString(rs.getString("listing_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("artifact_id")),
                rs.getString("artifact_data"),
                AuctionType.valueOf(rs.getString("type")),
                rs.getLong("price"),
                rs.getLong("current_bid"),
                currentBidderId,
                rs.getLong("created_at"),
                rs.getLong("expires_at")
        );
    }
}
