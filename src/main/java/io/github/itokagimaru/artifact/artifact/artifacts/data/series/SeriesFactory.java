package io.github.itokagimaru.artifact.artifact.artifacts.data.series;

import io.github.itokagimaru.artifact.Player.status.EffectSource;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.Effect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.ActionStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.*;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions.*;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Calculator;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class SeriesFactory {
    public static enum Key {
        SERIES("series"),
        SERIES_NAME("series-name"),
        MODEL("model"),
        FLAVOR_TEXT("flavor-text"),
        DESCRIPTION("description"),
        TEXT("text"),
        EX_STATUS("exception-status"),
        SET_EFFECT("set-effect"),
        TWO_SET("2set"),
        FOUR_SET("4set"),
        EFFECT("effects"),;
        public final String keyName;
        Key(String keyText){
            keyName = keyText;
        }
    }
    public static enum ExStatus {
        CANNOT_ENHANCE("cannot-enhance"),
        MAIN_EFFECT_FIXED("main-effect-fixed"),
        CANNOT_HAVE_SUB_EFFECT("cannot-have-sub-effect"),;
        public final String statusText;
        ExStatus(String statusText){
            this.statusText = statusText;
        }

        public static ExStatus fromText(String statusText) throws Exception {
            for(ExStatus status: ExStatus.values()){
                if(status.statusText.equals(statusText)){
                    return status;
                }
            }
            throw new IllegalArgumentException("ExStatusの値が不正です: " + statusText);
        }
    }
    public static enum effectKye {
        TRIGGER("trigger"),
        ACTIONS("actions"),
        CONDITIONS("conditions");
        public final String key;
        effectKye(String key){
            this.key = key;
        }
        public static effectKye fromText(String text) throws Exception {
            for(effectKye effect : effectKye.values()){
                if(effect.key.equals(text)){
                    return effect;
                }
            }
            throw new IllegalAccessException("effectsの値が不正です:" + text);
        }
    }
    public static enum TriggerKey {
        ON_UPDATE("on-artifact-update"),
        ON_DAMAGE("on-damage"),
        ON_ATTACK("on-attack"),
        ON_HEAL("on-heal"),
        ON_SKILL_USE("on-skill-use"),
        ON_CRITICAL_HIT("on-critical-hit");
        public final String text;
        TriggerKey(String keyText){
            text = keyText;
        }

        public String getText() {
            return text;
        }

        public static TriggerKey fromText(String text) throws Exception {
            for (TriggerKey rigger : values()) {
                if (rigger.text.equalsIgnoreCase(text)) {
                    return rigger;
                }
            }
            throw new IllegalAccessException("triggerの値が不正です:" + text);
        }
    }
    public static enum ConditionKey {
        OR_LASSE_HP("or-lass-hp"),
        OR_LASSE_ATK("or-lass-atk"),
        OR_LASSE_LUK("or-lass-luk"),
        OR_MORE_HP("or-more-hp"),
        OR_MORE_ATK("or-more-atk"),
        OR_MORE_LUK("or-more-luk"),
        IS_USED_SKILL("is-used-skill");
        public final String text;
        ConditionKey(String keyText){
            text = keyText;
        }

        public String getText() {
            return text;
        }

        public static ConditionKey fromText(String text) throws Exception {
            for (ConditionKey conditionKey : values()) {
                if (conditionKey.text.equalsIgnoreCase(text)) {
                    return conditionKey;
                }
            }
            throw new IllegalAccessException("Conditionの値が不正です: " + text);
        }
    }
    public static enum ActionKey {
        DO_GIVE_BUFF("do-give-buff"),
        DO_REMOVE_BUFF("do-remove-buff"),
        DO_GIVE_SKILL("do-give-skill"),
        DO_HEAL("do-heal"),
        DELAY("delay"),;
        public final String key;
        ActionKey(String keyText){
            key = keyText;
        }

        public static ActionKey fromText(String text) throws Exception {
            for (ActionKey actionKey : values()) {
                if (actionKey.key.equals(text)) {
                    return actionKey;
                }
            }
            throw new IllegalAccessException("actionの値が不正です:" + text);
        }
    }
    public static enum playerStatusKey {
        HP("player-status-hp"),
        ATK("player-status-atk"),
        DEF("player-status-def"),
        VIT("player-status-vit"),
        LUK("player-status-luk"),
        CRI("player-status-cri"),
        CRIDMG("player-status-cridmg"),
        FIER_DMG("player-status-fier-dmg-bonus"),
        WATER_DMG("player-status-water-dmg-bonus"),
        NATURE_DMG("player-status-nature-dmg-bonus"),
        FIER_REDUCE("player-status-fier-dmg-reduce"),
        WATER_REDUCE("player-status-water-dmg-reduce"),
        NATURE_REDUCE("player-status-nature-dmg-reduce");
        public final String statusText;
        playerStatusKey(String statusText){
            this.statusText = statusText;
        }
        public static playerStatusKey fromText(String statusText) throws Exception {
            for (playerStatusKey playerStatusKey : values()) {
                if (playerStatusKey.statusText.equalsIgnoreCase(statusText)) {
                    return playerStatusKey;
                }
            }
            throw new IllegalAccessException("playerStatusの値が不正です: " + statusText);
        }
    }
    public static Series makeSeries(YamlConfiguration config) throws Exception{

        ConfigurationSection seriesSec = config.getConfigurationSection(Key.SERIES.keyName);
        try {
            String seriesName = seriesSec.get(Key.SERIES_NAME.keyName).toString();
            String model = seriesSec.getString(Key.MODEL.keyName);
            List<Component> flavorText = toComponentText(seriesSec.getMapList(Key.FLAVOR_TEXT.keyName));
            ExceptionStatus.artifactExceptionStatus[] exStatus = toExStatusArray(seriesSec.getStringList(Key.EX_STATUS.keyName));
            ConfigurationSection setEffectSec = seriesSec.getConfigurationSection(Key.SET_EFFECT.keyName);
            ConfigurationSection twoSetEffectSec = setEffectSec.getConfigurationSection(Key.TWO_SET.keyName);
            List<Component> twoSetEffectDescription = toComponentText(twoSetEffectSec.getMapList(Key.DESCRIPTION.keyName));
            Effect[] twoSetEffect = toSetEffects(twoSetEffectSec, seriesName, 2);
            ConfigurationSection fourSetEffectSec = setEffectSec.getConfigurationSection(Key.FOUR_SET.keyName);
            List<Component> fourSetEffectDescription = toComponentText(fourSetEffectSec.getMapList(Key.DESCRIPTION.keyName));
            Effect[] fourSetEffect = toSetEffects(fourSetEffectSec, seriesName, 4);
            for (Effect effect : twoSetEffect){
                EffectStack.addEffect(effect);
            }
            for (Effect effect : fourSetEffect){
                EffectStack.addEffect(effect);
            }
            return new Series(seriesName, model, exStatus, twoSetEffectDescription, fourSetEffectDescription, flavorText);
        } catch (Exception e) {
            throw new IllegalAccessException("ymlファイルの読み込みに失敗しました: " + e.getMessage());
        }
    }

    private static ExceptionStatus.artifactExceptionStatus[] toExStatusArray(List<String> exStatusList) throws Exception {
        ExceptionStatus.artifactExceptionStatus[] exStatusArray = new ExceptionStatus.artifactExceptionStatus[exStatusList.toArray().length];
        for (int i = 0; i < exStatusList.toArray().length; i++){
            ExStatus exStatus = ExStatus.fromText(exStatusList.get(i));
            switch (exStatus){
                case ExStatus.CANNOT_ENHANCE -> {
                    exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.CANNOT_ENHANCE;
                }
                case ExStatus.MAIN_EFFECT_FIXED -> {
                    exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.MAIN_EFFECT_FIXED;
                }
                case ExStatus.CANNOT_HAVE_SUB_EFFECT -> {
                    exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.CANNOT_HAVE_SUB_EFFECT;
                }
                default -> {
                    throw new IllegalStateException("exStatusの値が不正です:" + exStatus);
                }
            }
        }
        return exStatusArray;
    }

    private static Effect[] toSetEffects(ConfigurationSection sec, String key, int setCount) throws Exception {
        List<Map<?, ?>> effectsList = sec.getMapList(Key.EFFECT.keyName);
        Effect[] effects = new Effect[effectsList.size()];
        for (int i = 0; i < effectsList.size(); i++){
            // TriggerKey
            String triggerRaw = effectsList.get(i).get(effectKye.TRIGGER.key).toString();

            // conditions
            List<Map<?, ?>> conditions = (List<Map<?, ?>>) effectsList.get(i).get(effectKye.CONDITIONS.key);

            // actions
            List<Map<?, ?>> actions = (List<Map<?, ?>>) effectsList.get(i).get(effectKye.ACTIONS.key);
            effects[i] = toSetEffect(triggerRaw, conditions, actions, key, setCount);
        }
        return effects;

    }

    private static Effect toSetEffect(String triggerRaw, List<Map<?, ?>> conditions, List<Map<?, ?>> actions, String key, int setCount) throws Exception {
        TriggerType.triggerType triggerType = toTriggerType(triggerRaw);
        Condition[] condition = toConditions(conditions);
        condition[condition.length -1] = new HasSeriesSet(key, setCount);
        Action[] actionStack = toActions(actions);
        return new Effect(triggerType, condition, actionStack);
    }

    private static TriggerType.triggerType toTriggerType(String triggerRaw) throws Exception {
        TriggerKey triggerId = TriggerKey.fromText(triggerRaw);
        switch (triggerId){
            case TriggerKey.ON_UPDATE -> {
                return TriggerType.triggerType.ON_UPDATE;
            }
            case TriggerKey.ON_DAMAGE -> {
                return TriggerType.triggerType.ON_DAMAGE;
            }
            case TriggerKey.ON_ATTACK -> {
                return TriggerType.triggerType.ON_ATTACK;
            }
            case TriggerKey.ON_HEAL -> {
                return TriggerType.triggerType.ON_HEAL;
            }
            case TriggerKey.ON_SKILL_USE -> {
                return TriggerType.triggerType.ON_SKILL_USE;
            }
            case TriggerKey.ON_CRITICAL_HIT -> {
                return TriggerType.triggerType.ON_CRITICAL_HIT;
            }
        }
        throw new IllegalAccessException("triggerの読み込みに失敗しました");
    }

    private static Condition[] toConditions(List<Map<?, ?>> conditionList) throws Exception {
        Condition[] conditions = new Condition[conditionList.toArray().length + 1];
        for (int i = 0; i < conditionList.toArray().length; i++){
            conditions[i] = toCondition(conditionList.get(i));
        }
        return conditions;
    }

    private static Condition toCondition(Map<?, ?> condition) throws Exception {
        for (Map.Entry<?, ?> entry : condition.entrySet()){
            String key = entry.getKey().toString();
            Map<?, ?> body = (Map<?, ?>) entry.getValue();
            Map<?, ?> valueMap = (Map<?, ?>) body.get("value");
            Values values = toValues(valueMap);
            boolean isMultiply = isMultiply(body.get("value-type").toString());
            ConditionKey conditionKey = ConditionKey.fromText(key);
            switch (conditionKey){
                case OR_MORE_HP -> {
                    return new OrMoreHp(values, isMultiply);
                }
                case OR_MORE_ATK ->  {
                    return new OrMoreAtk(values, isMultiply);
                }
                case OR_MORE_LUK -> {
                    return new OrMoreLuk(values, isMultiply);
                }
                case OR_LASSE_HP -> {
                    return new OrLessHp(values, isMultiply);
                }
                case OR_LASSE_ATK ->  {
                    return new OrLessAtk(values, isMultiply);
                }
                case OR_LASSE_LUK ->  {
                    return new OrLessLuk(values, isMultiply);
                }
                case IS_USED_SKILL -> {

                }
            }
        }
        throw new IllegalAccessException("Conditionの読み込みに失敗しました");
    }

    public static boolean isMultiply(String str) throws Exception {
        return Calculator.calculateType.MULTIPLY == Calculator.calculateType.fromText(str);
    }

    private static Action[] toActions(List<Map<?, ?>> actionsList) throws Exception {
        Action[] actions = new Action[actionsList.toArray().length];
        for (int i = 0; i < actionsList.toArray().length; i++){
            actions[i] = toAction(actionsList.get(i));
        }
        return actions;
    }

    private static Action toAction(Map<?, ?> actionMap) throws Exception {
        for (Map.Entry<?, ?> entry : actionMap.entrySet()){
            String key = entry.getKey().toString();
            Map<?, ?> body = (Map<?, ?>) entry.getValue();
            Map<?, ?> valueMap = (Map<?, ?>) body.get("value");
            Values values = toValues(valueMap);
            boolean isMultiply = isMultiply(body.get("value-type").toString());
            ActionKey actionKey = ActionKey.fromText(key);
            switch (actionKey){
                case DO_GIVE_BUFF -> {
                    return new DoGiveBuff(toPlayerStatus(body.get("player-status").toString()), values, isMultiply, body.get("key").toString(), EffectSource.EffectSourceType.SUB_EFFECT);
                }
                case DO_REMOVE_BUFF ->  {
                    return new DoRemoveBuff( new EffectSource(EffectSource.EffectSourceType.MAIN_EFFECT, key));
                }
                case DO_HEAL -> {
                    return new DoHeal();
                }
                case DO_GIVE_SKILL -> {
                    return new DoGiveSkill();
                }
                case DELAY ->  {
                    return new Delay(new ActionStack(toActions((List<Map<?,?>>) body.get("actions"))), values);
                }
            }
        }
        throw new Exception();
    }

    private static PlayerStatus.playerStatus toPlayerStatus(String str) throws Exception {
        switch (str){
            case "hp" -> {
                return PlayerStatus.playerStatus.HP;
            }
            case "atk" -> {
                return PlayerStatus.playerStatus.ATK;
            }
            case "def" -> {
                return PlayerStatus.playerStatus.DEF;
            }
            case "vit" -> {
                return PlayerStatus.playerStatus.VIT;
            }
            case "luk" -> {
                return PlayerStatus.playerStatus.LUK;
            }
            case "cri" -> {
                return PlayerStatus.playerStatus.CRI;
            }
            case "cridmg" -> {
                return PlayerStatus.playerStatus.CRIDMG;
            }
            case "fire-dmg" -> {
                return PlayerStatus.playerStatus.FIRE_DMG_BONUS;
            }
            case "water-dmg" -> {
                return PlayerStatus.playerStatus.WATER_DMG_BONUS;
            }
            case "nature-dmg" -> {
                return PlayerStatus.playerStatus.NATURE_DMG_BONUS;
            }
            case "fire-reduce" -> {
                return PlayerStatus.playerStatus.FIRE_DMG_REDUCE;
            }
            case "water-reduce" -> {
                return PlayerStatus.playerStatus.WATER_DMG_REDUCE;
            }
            case "nature-reduce" -> {
                return PlayerStatus.playerStatus.NATURE_DMG_REDUCE;
            }
        }
        throw new Exception();
    }

    private static Values toValues(Map<?, ?> valuesList){
        List<Map<Calculator.calculateType, String>> values = new ArrayList<>();
        String base = valuesList.get("base").toString();
        List<Map<Calculator.calculateType, String>> calc = toCalc((List<Map<?, ?>>) valuesList.get("calc"));
        return new Values(base, calc);
    }

    private static List<Map<Calculator.calculateType, String>> toCalc(List<Map<?, ?>> calcList){
        List<Map<Calculator.calculateType, String>> calcs = new ArrayList<>();
        for (Map<?, ?> map : calcList){
            for (Map.Entry<?, ?> entry : map.entrySet()){
                Map<Calculator.calculateType, String> calc = new HashMap<>(Map.of(Calculator.calculateType.fromText(entry.getKey().toString()), entry.getValue().toString()));
                calcs.add(calc);
            }
        }
        return calcs;
    }

    private static List<Component> toComponentText(List<Map<?, ?>> texts){
        List<Component> ComponentText = new ArrayList<>();
        for (Map<?, ?> text : texts){
            ComponentText.add(Component.text((String) text.get(Key.TEXT.keyName)));
        }
        return ComponentText;

    }


}
