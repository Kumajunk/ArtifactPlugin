package io.github.itokagimaru.artifact.auction.search;

/**
 * 検索結果のソート順序を表すenum
 */
public enum SortOrder {
    /** 価格昇順（安い順） */
    PRICE_ASC("価格（安い順）", "price", true),
    
    /** 価格降順（高い順） */
    PRICE_DESC("価格（高い順）", "price", false),
    
    /** 強化レベル昇順 */
    LEVEL_ASC("レベル（低い順）", "level", true),
    
    /** 強化レベル降順 */
    LEVEL_DESC("レベル（高い順）", "level", false),
    
    /** Main効果数値昇順 */
    MAIN_EFFECT_VALUE_ASC("Main効果（低い順）", "main_effect_value", true),
    
    /** Main効果数値降順 */
    MAIN_EFFECT_VALUE_DESC("Main効果（高い順）", "main_effect_value", false),
    
    /** 出品日時昇順（古い順） */
    CREATED_AT_ASC("出品日（古い順）", "created_at", true),
    
    /** 出品日時降順（新しい順） */
    CREATED_AT_DESC("出品日（新しい順）", "created_at", false),
    
    /** 残り時間昇順（終了が近い順） */
    EXPIRES_AT_ASC("終了（近い順）", "expires_at", true),
    
    /** 残り時間降順（終了が遠い順） */
    EXPIRES_AT_DESC("終了（遠い順）", "expires_at", false);

    private final String displayName;
    private final String columnName;
    private final boolean ascending;

    SortOrder(String displayName, String columnName, boolean ascending) {
        this.displayName = displayName;
        this.columnName = columnName;
        this.ascending = ascending;
    }

    /**
     * 表示用の名前を取得
     * @return 日本語でのソート名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * SQLカラム名を取得
     * @return カラム名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 昇順かどうか
     * @return 昇順の場合true
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * SQL ORDER BY句を生成
     * @return "column_name ASC" または "column_name DESC"
     */
    public String toSqlOrderBy() {
        return columnName + (ascending ? " ASC" : " DESC");
    }
}
