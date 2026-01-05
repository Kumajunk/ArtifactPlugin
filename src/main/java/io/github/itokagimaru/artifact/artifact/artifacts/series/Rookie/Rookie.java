package io.github.itokagimaru.artifact.artifact.artifacts.series.Rookie;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;

public class Rookie extends BaseArtifact {
    public Rookie(){
        artifactId = UUID.randomUUID();
        series = Series.artifactSeres.ROOKIE;
        seriesName = "駆け出し者";
        model = "rookie";
        exStatus = new ExceptionStatus.artifactExceptionStatus[]{ExceptionStatus.artifactExceptionStatus.CANNOT_ENHANCE, ExceptionStatus.artifactExceptionStatus.MAIN_EFFECT_FIXED, ExceptionStatus.artifactExceptionStatus.CANNOT_HAVE_SUB_EFFECT};
        flavorText = List.of(
                Component.text("初心者冒険者が最初に手にするアーティファクト").color(NamedTextColor.WHITE),
                Component.text("これからの冒険に対する期待に応えるかのように輝いている").color(NamedTextColor.WHITE)
        );
    }

}
