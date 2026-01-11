package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Values {
    List<Map<Calculator.calculateType, String>> values = new ArrayList<>();
    public Values(String base, List<Map<Calculator.calculateType, String>> values){
        this.values = values;
    }
    String base;

    public double calculate(UUID playerUuid) {
        double resultValue = getValue(playerUuid, base);
        for (Map<Calculator.calculateType, String> value : values){
            if(!value.get(Calculator.calculateType.ADD).isEmpty()){
                resultValue += getValue(playerUuid, value.get(Calculator.calculateType.ADD));
            } else if(!value.get(Calculator.calculateType.MULTIPLY).isEmpty()){
                resultValue *= getValue(playerUuid, value.get(Calculator.calculateType.MULTIPLY));
            }
        }
        return resultValue;
    }

    public double getValue(UUID playerUuid, String strValue) {
        double value = 0;
        try {
            value += Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            try {
                SeriesFactory.playerStatusKey playerStatusKey = SeriesFactory.playerStatusKey.fromText(strValue);
                switch (playerStatusKey) {
                    case SeriesFactory.playerStatusKey.HP -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.HP);
                    }
                    case SeriesFactory.playerStatusKey.ATK -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.ATK);
                    }
                    case SeriesFactory.playerStatusKey.DEF -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.DEF);
                    }
                    case SeriesFactory.playerStatusKey.VIT -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.VIT);
                    }
                    case SeriesFactory.playerStatusKey.LUK -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.LUK);
                    }
                    case SeriesFactory.playerStatusKey.CRI -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.CRI);
                    }
                    case SeriesFactory.playerStatusKey.CRIDMG -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.CRIDMG);
                    }
                    case SeriesFactory.playerStatusKey.FIER_DMG -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.FIRE_DMG_BONUS);
                    }
                    case SeriesFactory.playerStatusKey.WATER_DMG -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.WATER_DMG_BONUS);
                    }
                    case SeriesFactory.playerStatusKey.NATURE_DMG -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.NATURE_DMG_BONUS);
                    }
                    case SeriesFactory.playerStatusKey.FIER_REDUCE -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.FIRE_DMG_REDUCE);
                    }
                    case SeriesFactory.playerStatusKey.WATER_REDUCE -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.WATER_DMG_REDUCE);
                    }
                    case SeriesFactory.playerStatusKey.NATURE_REDUCE -> {
                        value += PlayerStatusManager.getPlayerStatus(playerUuid).getStatus(PlayerStatus.playerStatus.NATURE_DMG_REDUCE);
                    }
                }
            } catch (Exception exception){}
        }

        return value;
    }
}
