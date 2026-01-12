package io.github.itokagimaru.artifact.artifact.artifacts.data.series;

import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import net.kyori.adventure.text.Component;

import java.util.List;


public class Series {
    String internalName; // 内部名（レジストリのキーとして使用）
    String seriesName;   // 表示名
    String model;
    ExceptionStatus.artifactExceptionStatus[] exStatus;
    List<Component> twoSetDescription;
    List<Component> fourSerDescription;
    List<Component> flavorText;


    Series(String internalName, String seriesName, String model, ExceptionStatus.artifactExceptionStatus[] exStatus, List<Component> twoSetDescription, List<Component> fourSerDescription, List<Component> flavorText){
        this.internalName = internalName;
        this.seriesName = seriesName;
        this.model = model;
        this.exStatus = exStatus;
        this.twoSetDescription = twoSetDescription;
        this.fourSerDescription = fourSerDescription;
        this.flavorText = flavorText;
    }

    /**
     * 内部名を取得
     * @return 内部名（レジストリのキー）
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * 表示名を取得
     * @return 表示用のシリーズ名
     */
    public String getSeriesName() {
        return seriesName;
    }
    public String getModel() {
        return model;
    }
    public ExceptionStatus.artifactExceptionStatus[] getExStatus() {
        return exStatus;
    }
    public List<Component> getTwoSetDescription() {
        return twoSetDescription;
    }
    public List<Component> getFourSerDescription() {
        return fourSerDescription;
    }

    public List<Component> getFlavorText() {
        return flavorText;
    }
}
