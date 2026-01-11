package io.github.itokagimaru.artifact.artifact;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class EquipPdc {
    public static void saveToPdc(Player player, Slot.artifactSlot slot, BaseArtifact artifact) {
        // 1. スロットごとに固有のキーを作成 (例: artifact_slot_0)
        NamespacedKey key = new NamespacedKey(ArtifactMain.getInstance(), "artifact_slot_" + slot.getId);

        // 2. アーティファクトをJSON文字列に変換
        String json = JsonConverter.serializeArtifact(artifact);

        // 3. プレイヤーのPDCに文字列として保存
        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, json);
    }

    public static void removeFromPdc(Player player, Slot.artifactSlot slot) {
        NamespacedKey key = new NamespacedKey(ArtifactMain.getInstance(), "artifact_slot_" + slot.getId);
        // PDCからデータを削除
        player.getPersistentDataContainer().remove(key);
    }

    public static BaseArtifact loadFromPdc(Player player, Slot.artifactSlot slot) {
        NamespacedKey key = new NamespacedKey(ArtifactMain.getInstance(), "artifact_slot_" + slot.getId);
        String json = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (json != null) {
            return JsonConverter.deserializeArtifact(json);
        }
        return null;
    }
}
