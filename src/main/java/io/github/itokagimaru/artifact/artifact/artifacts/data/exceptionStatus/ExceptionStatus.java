package io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;

import java.util.HashMap;

public class ExceptionStatus {
    public static enum artifactExceptionStatus{
        CANNOT_ENHANCE(0, "※強化不可"),
        MAIN_EFFECT_FIXED(1, "※Main効果固定"),
        CANNOT_HAVE_SUB_EFFECT(2, "※Sub効果を付与できません");
        public final int getId;
        public final String getDescription;
        artifactExceptionStatus(int id, String description){
            this.getId = id;
            this.getDescription = description;
        }
        public static final HashMap<Integer,artifactExceptionStatus> exceptionStatusHashMap = new HashMap<>();
        static {
            for (artifactExceptionStatus exStatus : values()){
                exceptionStatusHashMap.put(exStatus.getId,exStatus);
            }
        }
        public static artifactExceptionStatus fromId(int value){
            return exceptionStatusHashMap.get(value);
        }
    }

    public static boolean isHaveExceptionStatus(Series seres, artifactExceptionStatus status){
        for (artifactExceptionStatus exStatus : seres.getExStatus()){
            if (exStatus == status) return true;
        }
        return false;
    }
}
