package io.github.itokagimaru.artifact.artifact.decompose;

import org.bukkit.inventory.ItemStack;

/**
 * ルートテーブルの1エントリを表すデータクラス
 */
public class LootEntry {
    private ItemStack item;
    private double chance;
    private int minAmount;
    private int maxAmount;

    /**
     * コンストラクタ
     *
     * @param item      ドロップアイテム
     * @param chance    ドロップ確率（0.0〜1.0）
     * @param minAmount 最小ドロップ数
     * @param maxAmount 最大ドロップ数
     */
    public LootEntry(ItemStack item, double chance, int minAmount, int maxAmount) {
        this.item = item;
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = Math.max(0.0, Math.min(1.0, chance));
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = Math.max(1, minAmount);
        if (this.maxAmount < this.minAmount) {
            this.maxAmount = this.minAmount;
        }
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = Math.max(this.minAmount, maxAmount);
    }

    /**
     * ランダムな数量を取得
     *
     * @return minAmountからmaxAmountの間のランダムな数
     */
    public int getRandomAmount() {
        if (minAmount == maxAmount) {
            return minAmount;
        }
        return minAmount + (int) (Math.random() * (maxAmount - minAmount + 1));
    }

    /**
     * 確率判定を行い、ドロップするかどうかを返す
     *
     * @return ドロップする場合true
     */
    public boolean shouldDrop() {
        return Math.random() < chance;
    }
}
