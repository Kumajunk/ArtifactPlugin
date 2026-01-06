package io.github.itokagimaru.artifact.auction.model;

/**
 * オークション種別を表すenum
 * - BIN: 即時購入（Buy It Now）
 * - AUCTION: 競売形式
 */
public enum AuctionType {
    /**
     * 即時購入方式
     * 出品価格で即座に購入が成立する
     */
    BIN("即時購入"),

    /**
     * 競売方式
     * 入札を行い、終了時に最高入札者が落札する
     */
    AUCTION("オークション");

    private final String displayName;

    AuctionType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 表示用の名前を取得
     * @return 日本語での種別名
     */
    public String getDisplayName() {
        return displayName;
    }
}
