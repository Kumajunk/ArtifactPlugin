package io.github.itokagimaru.artifact.auction.model;

import java.util.UUID;

/**
 * オークション出品情報を表すデータクラス
 * 1つの出品に関するすべての情報を保持する。
 * アーティファクトデータはJSON形式でシリアライズして保存し、
 * クラス構造変更時の互換性を確保する。
 */
public class AuctionListing {
    
    /** 出品固有ID（主キー） */
    private final UUID listingId;
    
    /** 出品者のUUID */
    private final UUID sellerId;
    
    /** アーティファクトのUUID（BaseArtifact.getUUID()） */
    private final UUID artifactId;
    
    /** アーティファクトのシリアライズデータ（JSON形式） */
    private final String artifactData;
    
    /** オークション種別（BIN / AUCTION） */
    private final AuctionType type;
    
    /** 出品価格（BIN）または開始価格（AUCTION） */
    private final long price;
    
    /** 現在の入札額（AUCTIONのみ使用、BINでは0） */
    private long currentBid;
    
    /** 現在の最高入札者UUID（AUCTIONのみ使用、入札がない場合はnull） */
    private UUID currentBidderId;
    
    /** 出品日時（Unix時間ミリ秒） */
    private final long createdAt;
    
    /** 終了日時（Unix時間ミリ秒） */
    private final long expiresAt;

    /**
     * 新規出品を作成するコンストラクタ
     * 
     * @param sellerId 出品者UUID
     * @param artifactId アーティファクトUUID
     * @param artifactData シリアライズされたアーティファクトデータ
     * @param type オークション種別
     * @param price 出品価格または開始価格
     * @param durationMillis 出品期間（ミリ秒）
     */
    public AuctionListing(UUID sellerId, UUID artifactId, String artifactData,
                          AuctionType type, long price, long durationMillis) {
        this.listingId = UUID.randomUUID();
        this.sellerId = sellerId;
        this.artifactId = artifactId;
        this.artifactData = artifactData;
        this.type = type;
        this.price = price;
        this.currentBid = 0;
        this.currentBidderId = null;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + durationMillis;
    }

    /**
     * DBからの復元用コンストラクタ
     */
    public AuctionListing(UUID listingId, UUID sellerId, UUID artifactId, String artifactData,
                          AuctionType type, long price, long currentBid, UUID currentBidderId,
                          long createdAt, long expiresAt) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.artifactId = artifactId;
        this.artifactData = artifactData;
        this.type = type;
        this.price = price;
        this.currentBid = currentBid;
        this.currentBidderId = currentBidderId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // ========== Getter ==========

    public UUID getListingId() {
        return listingId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public UUID getArtifactId() {
        return artifactId;
    }

    public String getArtifactData() {
        return artifactData;
    }

    public AuctionType getType() {
        return type;
    }

    public long getPrice() {
        return price;
    }

    public long getCurrentBid() {
        return currentBid;
    }

    public UUID getCurrentBidderId() {
        return currentBidderId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    // ========== Setter（入札更新用） ==========

    /**
     * 入札情報を更新する
     * 
     * @param bidderId 入札者UUID
     * @param bidAmount 入札額
     */
    public void updateBid(UUID bidderId, long bidAmount) {
        this.currentBidderId = bidderId;
        this.currentBid = bidAmount;
    }

    // ========== ユーティリティメソッド ==========

    /**
     * 出品が期限切れかどうかを判定
     * 
     * @return 期限切れの場合true
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    /**
     * 残り時間を取得（ミリ秒）
     * 
     * @return 残り時間（期限切れの場合は0）
     */
    public long getRemainingTime() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * キャンセル可能かどうかを判定（24時間以内のみ可能）
     * 
     * @param cancelWindowMillis キャンセル可能期間（ミリ秒）
     * @return キャンセル可能な場合true
     */
    public boolean isCancellable(long cancelWindowMillis) {
        long elapsed = System.currentTimeMillis() - createdAt;
        return elapsed <= cancelWindowMillis;
    }

    /**
     * 表示用の価格を取得
     * BINの場合は出品価格、AUCTIONの場合は現在入札額（なければ開始価格）
     * 
     * @return 表示用価格
     */
    public long getDisplayPrice() {
        if (type == AuctionType.AUCTION && currentBid > 0) {
            return currentBid;
        }
        return price;
    }
}
