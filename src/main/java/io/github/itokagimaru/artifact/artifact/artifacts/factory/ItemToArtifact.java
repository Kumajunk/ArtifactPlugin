package io.github.itokagimaru.artifact.artifact.artifacts.factory;

import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.data.ItemData;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

/**
 * ItemStackからBaseArtifactを復元するファクトリクラス
 */
public class ItemToArtifact {

    /**
     * ItemStackがアーティファクトかどうかを判定する
     * 
     * @param item 判定するアイテム
     * @return アーティファクトの場合true
     */
    public static boolean isArtifact(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // UUIDが設定されているかで判定
        String uuid = ItemData.UUID.get(item);
        return uuid != null && !uuid.isEmpty();
    }

    /**
     * ItemStackからBaseArtifactを復元する
     * 
     * @param item 変換するアイテム
     * @return BaseArtifact（変換失敗時はempty）
     */
    public static Optional<BaseArtifact> convert(ItemStack item) {
        if (!isArtifact(item)) {
            return Optional.empty();
        }

        try {
            // PDCからデータを取得
            String uuidStr = ItemData.UUID.get(item);
            int seriesId = ItemData.SERIES_ID.get(item);
            int slotId = ItemData.SLOT.get(item);
            int tierId = ItemData.TIER.get(item);
            int level = ItemData.LV.get(item);
            int mainEffectId = ItemData.MAIN_ID.get(item);
            double mainEffectValue = ByteArrayConverter.ByteToDouble(ItemData.MAIN_VALUE.get(item));
            int[] subEffectIds = ItemData.SUB_ID.get(item);
            double[] subEffectValues = ByteArrayConverter.ByteToDoubleArray(ItemData.SUB_VALUE.get(item));

            // シリーズからBaseArtifactを生成
            Series.artifactSeres series = Series.artifactSeres.fromId(seriesId);
            if (series == null) {
                return Optional.empty();
            }

            // シリーズに対応するアーティファクトインスタンスを取得
            BaseArtifact artifact = series.getArtifactType();
            if (artifact == null) {
                return Optional.empty();
            }

            // UUIDを設定（リフレクションで設定）
            try {
                var uuidField = BaseArtifact.class.getDeclaredField("artifactId");
                uuidField.setAccessible(true);
                uuidField.set(artifact, UUID.fromString(uuidStr));
            } catch (Exception e) {
                // UUIDの設定に失敗しても続行
            }

            // その他のステータスを設定
            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            Tier.artifactTier tier = Tier.artifactTier.fromId(tierId);
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);

            // Sub効果を配列に変換
            SubEffect.artifactSubEffect[] subEffects = new SubEffect.artifactSubEffect[4];
            for (int i = 0; i < 4 && i < subEffectIds.length; i++) {
                if (subEffectIds[i] >= 0) {
                    subEffects[i] = SubEffect.artifactSubEffect.fromId(subEffectIds[i]);
                }
            }

            // ステータスを設定
            artifact.setStatus(slot, tier, level, mainEffect, mainEffectValue, subEffects, subEffectValues);

            return Optional.of(artifact);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * アーティファクトのUUIDを取得する
     * 
     * @param item アイテム
     * @return UUID（取得失敗時はnull）
     */
    public static UUID getArtifactId(ItemStack item) {
        if (!isArtifact(item)) {
            return null;
        }
        try {
            String uuidStr = ItemData.UUID.get(item);
            return UUID.fromString(uuidStr);
        } catch (Exception e) {
            return null;
        }
    }
}
