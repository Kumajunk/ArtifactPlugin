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
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Value;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value.Values;
import io.github.itokagimaru.artifact.artifact.artifacts.data.exceptionStatus.ExceptionStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class SeriesFactory {
    public enum Key {
        SERIES("series"),
        INTERNAL_NAME("internal-name"),
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
    public enum ExStatus {
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
    public enum effectKey {
        TRIGGER("trigger"),
        ACTIONS("actions"),
        CONDITIONS("conditions");
        public final String key;
        effectKey(String key){
            this.key = key;
        }
        public static effectKey fromText(String text) throws Exception {
            for(effectKey effect : effectKey.values()){
                if(effect.key.equals(text)){
                    return effect;
                }
            }
            throw new IllegalAccessException("effectsの値が不正です:" + text);
        }
    }
    public enum TriggerKey {
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
    public enum ConditionKey {
        OR_LESS_HP("or-less-hp"),
        OR_LESS_ATK("or-less-atk"),
        OR_LESS_LUK("or-less-luk"),
        OR_LESS_PDC("or-less-pdc"),
        OR_MORE_HP("or-more-hp"),
        OR_MORE_ATK("or-more-atk"),
        OR_MORE_LUK("or-more-luk"),
        OR_MORE_PDC("or-more-pdc"),
        IS_USED_SKILL("is-used-skill"),
        IS_TRUE("is-true");
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
    public enum ActionKey {
        DO_GIVE_BUFF("do-give-buff"),
        DO_REMOVE_BUFF("do-remove-buff"),
        DO_GIVE_SKILL("do-give-skill"),
        DO_REMOVE_SKILL("do-remove-skill"),
        DO_HEAL("do-heal"),
        DELAY("delay"),
        DO_SET_PDC("do-set-pdc"),
        DO_ADD_PDC("do-add-pdc"),;
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
    public enum playerStatusKey {
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
            String internalName = seriesSec.getString(Key.INTERNAL_NAME.keyName);
            String seriesName = seriesSec.get(Key.SERIES_NAME.keyName).toString();
            String model = seriesSec.getString(Key.MODEL.keyName);
            List<Component> flavorText = toComponentText(seriesSec.getMapList(Key.FLAVOR_TEXT.keyName));
            ExceptionStatus.artifactExceptionStatus[] exStatus = toExStatusArray(seriesSec.getStringList(Key.EX_STATUS.keyName));
            ConfigurationSection setEffectSec = seriesSec.getConfigurationSection(Key.SET_EFFECT.keyName);
            ConfigurationSection twoSetEffectSec = setEffectSec.getConfigurationSection(Key.TWO_SET.keyName);
            List<Component> twoSetEffectDescription = toComponentText(twoSetEffectSec.getMapList(Key.DESCRIPTION.keyName));
            Effect[] twoSetEffect = toSetEffects(twoSetEffectSec, internalName, 2);
            ConfigurationSection fourSetEffectSec = setEffectSec.getConfigurationSection(Key.FOUR_SET.keyName);
            List<Component> fourSetEffectDescription = toComponentText(fourSetEffectSec.getMapList(Key.DESCRIPTION.keyName));
            Effect[] fourSetEffect = toSetEffects(fourSetEffectSec, internalName, 4);
            for (Effect effect : twoSetEffect){
                EffectStack.addEffect(effect);
            }
            for (Effect effect : fourSetEffect){
                EffectStack.addEffect(effect);
            }
            return new Series(internalName, seriesName, model, exStatus, twoSetEffectDescription, fourSetEffectDescription, flavorText);
        } catch (Exception e) {
            throw new IllegalAccessException(e.getMessage());
        }
    }

    private static ExceptionStatus.artifactExceptionStatus[] toExStatusArray(List<String> exStatusList) throws Exception {
        ExceptionStatus.artifactExceptionStatus[] exStatusArray = new ExceptionStatus.artifactExceptionStatus[exStatusList.toArray().length];
        for (int i = 0; i < exStatusList.toArray().length; i++){
            ExStatus exStatus = ExStatus.fromText(exStatusList.get(i));
            switch (exStatus){
                case ExStatus.CANNOT_ENHANCE -> exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.CANNOT_ENHANCE;
                case ExStatus.MAIN_EFFECT_FIXED -> exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.MAIN_EFFECT_FIXED;
                case ExStatus.CANNOT_HAVE_SUB_EFFECT -> exStatusArray[i] = ExceptionStatus.artifactExceptionStatus.CANNOT_HAVE_SUB_EFFECT;
                default -> throw new IllegalStateException("exStatusの値が不正です:" + exStatus);
            }
        }
        return exStatusArray;
    }

    private static Effect[] toSetEffects(ConfigurationSection sec, String key, int setCount) throws Exception {
        List<Map<?, ?>> effectsList = sec.getMapList(Key.EFFECT.keyName);
        Effect[] effects = new Effect[effectsList.size()];
        for (int i = 0; i < effectsList.size(); i++){
            // TriggerKey
            String triggerRaw = effectsList.get(i).get(effectKey.TRIGGER.key).toString();

            // conditions
            List<Map<?, ?>> conditions = (List<Map<?, ?>>) effectsList.get(i).get(effectKey.CONDITIONS.key);

            // actions
            List<Map<?, ?>> actions = (List<Map<?, ?>>) effectsList.get(i).get(effectKey.ACTIONS.key);
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
            Map<?, ?> conditionBody = (Map<?, ?>) entry.getValue();

            ConditionKey conditionKey = ConditionKey.fromText(key);
            switch (conditionKey){
                case OR_MORE_HP -> {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrMoreHp(values, isMultiply);
                }
                case OR_MORE_ATK ->  {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrMoreAtk(values, isMultiply);
                }
                case OR_MORE_LUK -> {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrMoreLuk(values, isMultiply);
                }
                case OR_MORE_PDC -> {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    return new OrMorePdc(values, conditionBody.get("key").toString());
                }
                case OR_LESS_HP -> {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrLessHp(values, isMultiply);
                }
                case OR_LESS_ATK ->  {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrLessAtk(values, isMultiply);
                }
                case OR_LESS_LUK ->  {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(conditionBody.get("value-type").toString());
                    return new OrLessLuk(values, isMultiply);
                }
                case OR_LESS_PDC -> {
                    Map<?, ?> valueMap = (Map<?, ?>) conditionBody.get("value");
                    Values values = toValues(valueMap);
                    return new OrLessPdc(values, conditionBody.get("key").toString());
                }
                case IS_USED_SKILL -> {
                    return new IsUseSkill(conditionBody.get("key").toString());
                }
                case IS_TRUE -> {
                    return new IsTrue();
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
            String rawActionKey = entry.getKey().toString();
            Map<?, ?> actionBody = (Map<?, ?>) entry.getValue();
            ActionKey actionKey = ActionKey.fromText(rawActionKey);
            switch (actionKey){
                case DO_GIVE_BUFF -> {
                    Map<?, ?> valueMap = (Map<?, ?>) actionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(actionBody.get("value-type").toString());
                    return new DoGiveBuff(toPlayerStatus(actionBody.get("player-status").toString()), values, isMultiply, actionBody.get("key").toString(), EffectSource.EffectSourceType.MAIN_EFFECT);
                }
                case DO_REMOVE_BUFF ->  {
                    String removeTypeStr = actionBody.get("remove-type").toString();
                    DoRemoveBuff.RemoveType removeType;
                    switch (removeTypeStr){
                        case "all" -> {
                            removeType = DoRemoveBuff.RemoveType.ALL;
                        }
                        case "each" -> {
                            removeType = DoRemoveBuff.RemoveType.EACH;
                        }
                        default -> {
                            throw new IllegalAccessException("Do-remove-buffのremove-typeの値が不正です");
                        }
                    }
                    return new DoRemoveBuff(new EffectSource(EffectSource.EffectSourceType.MAIN_EFFECT, actionBody.get("key").toString()), removeType);
                }
                case DO_HEAL -> {
                    Map<?, ?> valueMap = (Map<?, ?>) actionBody.get("value");
                    Values values = toValues(valueMap);
                    boolean isMultiply = isMultiply(actionBody.get("value-type").toString());
                    return new DoHeal(values, isMultiply);
                }
                case DO_GIVE_SKILL -> {
                    return new DoGiveSkill(actionBody.get("key").toString(), actionBody.get("skill-name").toString(), actionBody.get("model").toString(), toComponentText((List<Map<?, ?>>) actionBody.get(Key.DESCRIPTION.keyName)));
                }
                case DO_REMOVE_SKILL ->  {
                    return new DoRemoveSkill(actionBody.get("key").toString());
                }
                case DELAY ->  {
                    Map<?, ?> valueMap = (Map<?, ?>) actionBody.get("value");
                    Values values = toValues(valueMap);
                    return new Delay(new ActionStack(toActions((List<Map<?,?>>) actionBody.get(effectKey.ACTIONS.key))), values);
                }
                case DO_SET_PDC -> {
                    Map<?, ?> valueMap = (Map<?, ?>) actionBody.get("value");
                    Values values = toValues(valueMap);
                    return new DoSetPDC(values, actionBody.get("key").toString());
                }
                case DO_ADD_PDC -> {
                    Map<?, ?> valueMap = (Map<?, ?>) actionBody.get("value");
                    Values values = toValues(valueMap);
                    return new DoAddPDC(values, actionBody.get("key").toString());
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
        String base = valuesList.get("base").toString();
        List<Value> calc = toCalc((List<Map<?, ?>>) valuesList.get("calc"));
        return new Values(base, calc);
    }

    private static List<Value> toCalc(List<Map<?, ?>> calcList){
        List<Value> calcs = new ArrayList<>();
        for (Map<?, ?> map : calcList){
            for (Map.Entry<?, ?> entry : map.entrySet()){
                Value calc = new Value(entry.getValue().toString(),Calculator.calculateType.fromText(entry.getKey().toString()));
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
