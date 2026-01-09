package io.github.itokagimaru.artifact.artifact.artifacts.series.King;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;

public class King extends BaseArtifact {
    public King() {
        artifactId = UUID.randomUUID();
        series = Series.artifactSeres.KING;
        seriesName = "王の遺志";
        model = "rookie";
        exStatus = new ExceptionStatus.artifactExceptionStatus[]{};
        flavorText = List.of(
                Component.text("かつて大陸を統一した覇王が愛用したとされる宝具。").color(NamedTextColor.GOLD),
                Component.text("その輝きは今なお失われておらず、手にする者に王の威光を授ける。").color(NamedTextColor.GOLD)
        );
    }
}
