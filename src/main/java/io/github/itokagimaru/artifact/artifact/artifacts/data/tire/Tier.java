package io.github.itokagimaru.artifact.artifact.artifacts.data.tire;

import java.util.HashMap;

public class Tier {
    public enum artifactTier {
        SS(0, "SS"),
        S(1, "S"),
        A(2, "A"),
        B(3, "B"),
        C(4, "C");
        public final int getId;
        public final String getText;
        artifactTier(int id, String tire){
            this.getId = id;
            this.getText = tire;
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
    }
}
