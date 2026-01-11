package io.github.itokagimaru.artifact.artifact.artifacts.data.series;

import java.util.LinkedHashMap;
import java.util.Map;

public class SeriesRegistry {
    public static Map<String, Series> seriesRegistry = new LinkedHashMap<>();

    public static void addSeries(Series series){
        seriesRegistry.put(series.seriesName,series);
    }

    public static Series getSeries(String key){
        return seriesRegistry.get(key);
    }

    public static int getIndex(Series series){
        int index = 0;
        for (Series v : seriesRegistry.values()) {
            if (v.equals(series)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 全シリーズを配列で取得
     */
    public static Series[] getAllSeries() {
        return seriesRegistry.values().toArray(new Series[0]);
    }

    /**
     * インデックスからシリーズを取得
     */
    public static Series getSeriesByIndex(int index) {
        int i = 0;
        for (Series series : seriesRegistry.values()) {
            if (i == index) return series;
            i++;
        }
        return null;
    }
}
