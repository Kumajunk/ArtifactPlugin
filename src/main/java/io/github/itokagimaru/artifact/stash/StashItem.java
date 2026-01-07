package io.github.itokagimaru.artifact.stash;

import java.util.UUID;

/**
 * Stashに保存されたアイテムを表すモデルクラス
 */
public class StashItem {
    
    private final UUID id;
    private final UUID playerId;
    private final String itemData;  // シリアライズされたアーティファクトデータ（JSON）
    private final String source;    // アイテムの入手元（"auction_purchase", "auction_return" など）
    private final long createdAt;

    /**
     * 新規作成用コンストラクタ
     */
    public StashItem(UUID playerId, String itemData, String source) {
        this.id = UUID.randomUUID();
        this.playerId = playerId;
        this.itemData = itemData;
        this.source = source;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * DB復元用コンストラクタ
     */
    public StashItem(UUID id, UUID playerId, String itemData, String source, long createdAt) {
        this.id = id;
        this.playerId = playerId;
        this.itemData = itemData;
        this.source = source;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getItemData() {
        return itemData;
    }

    public String getSource() {
        return source;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * 入手元の表示名を取得
     */
    public String getSourceDisplayName() {
        return switch (source) {
            case "auction_purchase" -> "オークション購入";
            case "auction_return" -> "オークション返却";
            case "auction_win" -> "オークション落札";
            case "admin" -> "管理者付与";
            default -> "不明";
        };
    }
}
