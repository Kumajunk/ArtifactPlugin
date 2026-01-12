package io.github.itokagimaru.artifact.artifact;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;

import java.util.UUID;

import static io.github.itokagimaru.artifact.ArtifactMain.plugin;

public final class JsonConverter {
    private static final Gson gson = new Gson();

    /**
     * アーティファクトをJSON形式でシリアライズする
     */
    public static String serializeArtifact(BaseArtifact artifact) {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", artifact.getUUID().toString());
        json.addProperty("seriesInternalName", artifact.getSeries().getInternalName());
        json.addProperty("seriesName", artifact.getSeries().getSeriesName()); // 後方互換性のため保持
        json.addProperty("seriesIndex", SeriesRegistry.getIndex(artifact.getSeries())); // DB検索用インデックス
        json.addProperty("slotId", artifact.getSlot().getId);
        json.addProperty("tierId", artifact.getTier().getId);
        json.addProperty("level", artifact.getLv());
        json.addProperty("mainEffectId", artifact.getMainEffect().getId);
        json.addProperty("mainEffectValue", artifact.getMainEffectValue());

        // Sub効果のIDを配列として保存
        StringBuilder subIds = new StringBuilder();
        int count = 0;
        for (var subEffect : artifact.getSubEffects()) {
            if (subEffect != null) {
                if (count > 0) subIds.append(",");
                subIds.append(subEffect.getId);
                count++;
            }
        }
        json.addProperty("subEffectIds", subIds.toString());
        json.addProperty("subEffectCount", count);

        // Sub効果の値も保存
        StringBuilder subValues = new StringBuilder();
        double[] values = artifact.getSubEffectsValue();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) subValues.append(",");
            subValues.append(values[i]);
        }
        json.addProperty("subEffectValues", subValues.toString());

        return gson.toJson(json);
    }

    /**
     * JSONからアーティファクトをデシリアライズする
     */
    public static BaseArtifact deserializeArtifact(String artifactData) {
        try {
            JsonObject json = gson.fromJson(artifactData, JsonObject.class);

            // 内部名で検索（後方互換性のためseriesNameもフォールバック）
            String internalName = json.has("seriesInternalName") 
                ? json.get("seriesInternalName").getAsString() 
                : json.has("seriesName") ? json.get("seriesName").getAsString() : null;
            Series series = SeriesRegistry.getSeriesWithFallback(internalName);
            if (series == null) return null;

            BaseArtifact artifact = new BaseArtifact();

            // UUIDを設定
            String uuidStr = json.has("uuid") ? json.get("uuid").getAsString() : UUID.randomUUID().toString();
            try {
                var uuidField = BaseArtifact.class.getDeclaredField("artifactId");
                uuidField.setAccessible(true);
                uuidField.set(artifact, UUID.fromString(uuidStr));
            } catch (Exception ignored) {}

            // ステータスを設定
            int slotId = json.has("slotId") ? json.get("slotId").getAsInt() : 0;
            int tierId = json.has("tierId") ? json.get("tierId").getAsInt() : 0;
            int level = json.has("level") ? json.get("level").getAsInt() : 0;
            int mainEffectId = json.has("mainEffectId") ? json.get("mainEffectId").getAsInt() : 0;
            double mainEffectValue = json.has("mainEffectValue") ? json.get("mainEffectValue").getAsDouble() : 0;

            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            Tier.artifactTier tier = Tier.artifactTier.fromId(tierId);
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);

            // Sub効果を復元
            SubEffect.artifactSubEffect[] subEffects = new SubEffect.artifactSubEffect[4];
            double[] subEffectValues = new double[4];

            if (json.has("subEffectIds") && json.has("subEffectValues")) {
                String[] ids = json.get("subEffectIds").getAsString().split(",");
                String[] vals = json.get("subEffectValues").getAsString().split(",");

                for (int i = 0; i < ids.length && i < 4; i++) {
                    if (!ids[i].isEmpty()) {
                        try {
                            int subId = Integer.parseInt(ids[i].trim());
                            subEffects[i] = SubEffect.artifactSubEffect.fromId(subId);
                            if (i < vals.length && !vals[i].isEmpty()) {
                                subEffectValues[i] = Double.parseDouble(vals[i].trim());
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            artifact.setStatus(series, slot, tier, level, mainEffect, mainEffectValue, subEffects, subEffectValues);
            return artifact;

        } catch (Exception e) {
            plugin.getLogger().severe("Artifact deserialization error: " + e.getMessage());
            return null;
        }
    }
}
