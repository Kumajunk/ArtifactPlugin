package io.github.itokagimaru.artifact.artifact.artifacts.data.series;

import java.util.LinkedHashMap;
import java.util.Map;

public class SeriesRegistry {
    public static Map<String, Series> seriesRegistry = new LinkedHashMap<>();

    public static void addSeries(Series series){
        seriesRegistry.put(series.internalName, series);
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

    /**
     * キーまたは表示名でシリーズを検索する（後方互換性用フォールバック）
     * 内部名で見つからない場合は表示名でも検索する
     * 
     * @param key 検索キー（内部名または表示名）
     * @return シリーズ（見つからない場合はnull）
     */
    public static Series getSeriesWithFallback(String key) {
        // まず内部名で検索
        Series series = seriesRegistry.get(key);
        if (series != null) {
            return series;
        }
        
        // 見つからない場合は表示名で検索（後方互換性）
        for (Series s : seriesRegistry.values()) {
            if (s.getSeriesName().equals(key)) {
                return s;
            }
        }
        
        return null;
    }
}
