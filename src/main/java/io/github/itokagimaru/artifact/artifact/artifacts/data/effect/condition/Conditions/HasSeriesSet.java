package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import java.util.UUID;

public class HasSeriesSet extends Condition {
    String seriesName;
    int requiredCount;

    public HasSeriesSet(String seriesName, int requiredCount){
        this.seriesName = seriesName;
        this.requiredCount = requiredCount;
    }

    @Override
    public boolean isTrue(UUID playerUuid){
        return false;
    }


}
