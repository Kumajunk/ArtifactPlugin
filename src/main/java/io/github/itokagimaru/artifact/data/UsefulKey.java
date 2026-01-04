package io.github.itokagimaru.artifact.data;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;
import java.util.function.Supplier;

/// PDCのラッパークラス
@NullMarked
public abstract class UsefulKey<C> {
    public final NamespacedKey key;
    public final PersistentDataType<?, C> dataType;
    public final Supplier<C> defaultValue;

    public UsefulKey(NamespacedKey key, PersistentDataType<?, C> dataType, Supplier<C> defaultValue) {
        this.key = key;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    /// PDCの値を取得します。
    /// @param dataContainerView 対象のデータコンテナビュー
    /// @return データの値
    public C get(PersistentDataContainerView dataContainerView) {
        return dataContainerView.getOrDefault(this.key, this.dataType, this.defaultValue.get());
    }

    /// PDCの値を取得します。
    /// @param stack 対象のアイテムスタック
    /// @return データの値
    public C get(ItemStack stack) {
        return get(stack.getPersistentDataContainer());
    }

    /// PDCの値を設定します。
    /// @param dataContainer 対象のデータコンテナ
    /// @param newValue 新しい値
    public void set(PersistentDataContainer dataContainer, C newValue) {
        dataContainer.set(this.key, this.dataType, newValue);
    }

    /// PDCの値を設定します。
    /// @param stack 対象のアイテムスタック
    /// @param newValue 新しい値
    public void set(ItemStack stack, C newValue) {
        stack.editPersistentDataContainer(p -> set(p, newValue));
    }

    /// PDCの値を編集します。
    /// @param dataContainer 対象のデータコンテナ
    /// @param applyFunc 編集操作のファンクション
    void compute(PersistentDataContainer dataContainer, Function<C, C> applyFunc) {
        set(dataContainer, applyFunc.apply(get(dataContainer)));
    }

    /// PDCの値を編集します。
    /// @param stack 対象のアイテムスタック
    /// @param applyFunc 編集操作のファンクション
    void compute(ItemStack stack, Function<C, C> applyFunc) {
        stack.editMeta(meta -> compute(meta.getPersistentDataContainer(), applyFunc));
    }
}
