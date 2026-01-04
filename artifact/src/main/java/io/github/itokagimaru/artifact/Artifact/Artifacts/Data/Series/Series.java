package io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Series;

import io.github.itokagimaru.artifact.Artifact.Artifacts.series.Base.BaseArtifact;
import io.github.itokagimaru.artifact.Artifact.Artifacts.series.Rookie.Rookie;

import java.util.HashMap;
import java.util.function.Supplier;

public class Series {
    public static enum artifactSeres{
        ROOKIE(0, () -> new Rookie());
        public final int getId;
        private final Supplier<BaseArtifact> supplier;
        artifactSeres(int id, Supplier<BaseArtifact> supplier){
            this.getId = id;
            this.supplier = supplier;
        }
        public static final HashMap<Integer, artifactSeres> seresHashMap = new HashMap<>();
        static {
            for (artifactSeres seres : values()){
                seresHashMap.put(seres.getId,seres);
            }
        }
        public static artifactSeres fromId(int value){
            return seresHashMap.get(value);
        }

        public BaseArtifact getArtifactType(){
            return supplier.get();
        }
    }
}
