package io.github.itokagimaru.artifact.auction.search;

import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.model.AuctionType;

import java.util.ArrayList;
import java.util.List;

/**
 * オークション検索フィルターを表すビルダークラス
 * 
 * 各条件をメソッドチェーンで設定でき、
 * 未設定の条件はnullとしてスキップされる。
 */
public class AuctionSearchFilter {
    
    // === 基本条件 ===
    
    /** シリーズ条件（複数選択可） */
    private List<Series.artifactSeres> series = new ArrayList<>();
    
    /** スロット条件 */
    private Slot.artifactSlot slot;
    
    /** 強化レベル最小値 */
    private Integer levelMin;
    
    /** 強化レベル最大値 */
    private Integer levelMax;
    
    // === Main効果条件 ===
    
    /** Main効果の種類 */
    private MainEffect.artifactMainEffect mainEffect;
    
    /** Main効果の最小数値 */
    private Integer mainEffectValueMin;
    
    /** Main効果の最大数値 */
    private Integer mainEffectValueMax;
    
    // === Sub効果条件 ===
    
    /** 含まれるべきSub効果（AND条件） */
    private List<SubEffect.artifactSubEffect> requiredSubEffects = new ArrayList<>();
    
    /** 除外するSub効果（これらを含まない） */
    private List<SubEffect.artifactSubEffect> excludedSubEffects = new ArrayList<>();
    
    /** Sub効果の数（2〜4） */
    private Integer subEffectCount;
    
    // === 価格条件 ===
    
    /** 最小価格 */
    private Long priceMin;
    
    /** 最大価格 */
    private Long priceMax;
    
    // === オークション種別 ===
    
    /** BINのみ / AUCTIONのみ / 両方 */
    private AuctionType auctionType;
    
    // === キーワード検索 ===
    
    /** シリーズ名キーワード */
    private String keyword;

    // ========== ビルダーメソッド ==========

    /**
     * シリーズ条件を追加
     */
    public AuctionSearchFilter addSeries(Series.artifactSeres series) {
        this.series.add(series);
        return this;
    }

    /**
     * シリーズ条件を設定（複数）
     */
    public AuctionSearchFilter setSeries(List<Series.artifactSeres> series) {
        this.series = new ArrayList<>(series);
        return this;
    }

    /**
     * スロット条件を設定
     */
    public AuctionSearchFilter setSlot(Slot.artifactSlot slot) {
        this.slot = slot;
        return this;
    }

    /**
     * 強化レベル範囲を設定
     */
    public AuctionSearchFilter setLevelRange(Integer min, Integer max) {
        this.levelMin = min;
        this.levelMax = max;
        return this;
    }

    /**
     * Main効果条件を設定
     */
    public AuctionSearchFilter setMainEffect(MainEffect.artifactMainEffect mainEffect) {
        this.mainEffect = mainEffect;
        return this;
    }

    /**
     * Main効果数値範囲を設定
     */
    public AuctionSearchFilter setMainEffectValueRange(Integer min, Integer max) {
        this.mainEffectValueMin = min;
        this.mainEffectValueMax = max;
        return this;
    }

    /**
     * 含まれるべきSub効果を追加
     */
    public AuctionSearchFilter addRequiredSubEffect(SubEffect.artifactSubEffect subEffect) {
        this.requiredSubEffects.add(subEffect);
        return this;
    }

    /**
     * 除外するSub効果を追加
     */
    public AuctionSearchFilter addExcludedSubEffect(SubEffect.artifactSubEffect subEffect) {
        this.excludedSubEffects.add(subEffect);
        return this;
    }

    /**
     * Sub効果の数を設定
     */
    public AuctionSearchFilter setSubEffectCount(Integer count) {
        this.subEffectCount = count;
        return this;
    }

    /**
     * 価格範囲を設定
     */
    public AuctionSearchFilter setPriceRange(Long min, Long max) {
        this.priceMin = min;
        this.priceMax = max;
        return this;
    }

    /**
     * オークション種別を設定
     */
    public AuctionSearchFilter setAuctionType(AuctionType type) {
        this.auctionType = type;
        return this;
    }

    /**
     * キーワードを設定
     */
    public AuctionSearchFilter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    /**
     * フィルターをリセット
     */
    public AuctionSearchFilter reset() {
        this.series = new ArrayList<>();
        this.slot = null;
        this.levelMin = null;
        this.levelMax = null;
        this.mainEffect = null;
        this.mainEffectValueMin = null;
        this.mainEffectValueMax = null;
        this.requiredSubEffects = new ArrayList<>();
        this.excludedSubEffects = new ArrayList<>();
        this.subEffectCount = null;
        this.priceMin = null;
        this.priceMax = null;
        this.auctionType = null;
        this.keyword = null;
        return this;
    }

    // ========== Getter ==========

    public List<Series.artifactSeres> getSeries() {
        return series;
    }

    public Slot.artifactSlot getSlot() {
        return slot;
    }

    public Integer getLevelMin() {
        return levelMin;
    }

    public Integer getLevelMax() {
        return levelMax;
    }

    public MainEffect.artifactMainEffect getMainEffect() {
        return mainEffect;
    }

    public Integer getMainEffectValueMin() {
        return mainEffectValueMin;
    }

    public Integer getMainEffectValueMax() {
        return mainEffectValueMax;
    }

    public List<SubEffect.artifactSubEffect> getRequiredSubEffects() {
        return requiredSubEffects;
    }

    public List<SubEffect.artifactSubEffect> getExcludedSubEffects() {
        return excludedSubEffects;
    }

    public Integer getSubEffectCount() {
        return subEffectCount;
    }

    public Long getPriceMin() {
        return priceMin;
    }

    public Long getPriceMax() {
        return priceMax;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public String getKeyword() {
        return keyword;
    }

    /**
     * 条件が設定されているかどうか
     * @return いずれかの条件が設定されていればtrue
     */
    public boolean hasAnyCondition() {
        return !series.isEmpty() 
            || slot != null 
            || levelMin != null 
            || levelMax != null
            || mainEffect != null 
            || mainEffectValueMin != null 
            || mainEffectValueMax != null
            || !requiredSubEffects.isEmpty() 
            || !excludedSubEffects.isEmpty()
            || subEffectCount != null 
            || priceMin != null 
            || priceMax != null
            || auctionType != null 
            || keyword != null;
    }
}
