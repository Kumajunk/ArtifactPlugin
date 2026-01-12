package io.github.itokagimaru.artifact.artifact.artifacts.data.tier;

import io.github.itokagimaru.artifact.artifact.GeneralConfig;

import java.util.HashMap;

public class Tier {
    public enum artifactTier {
        SS(0, "SS", 1.0),
        S(1, "S", 1.0),
        A(2, "A", 1.0),
        B(3, "B", 1.0),
        C(4, "C", 1.0);
        public final int getId;
        public final String getText;
        public Double getPriceScale;
        artifactTier(int id, String tire, Double priceScale){
            this.getId = id;
            this.getText = tire;
            this.getPriceScale = priceScale;
        }
        public static final HashMap<Integer, artifactTier> tierHashMap = new HashMap<>();
        static {
            for (artifactTier tier : values()){
                tierHashMap.put(tier.getId, tier);
            }
        }
        public static artifactTier fromId(int id){
            return tierHashMap.get(id);
        }

        public static artifactTier fromTier(String tire){
            for (artifactTier tier : values()){
                if (tier.getText.equalsIgnoreCase(tire)){
                    return tier;
                }
            }
            return null;
        }

        public static void reloadTierPriceScale(GeneralConfig config) {
            for (artifactTier tier : values()) {
                if (config.isTierScaling()) {
                    tier.getPriceScale = config.getTierMultiplier(tier.getText);
                } else {
                    tier.getPriceScale = 1.0;
                }
            }
        }
    }
}
