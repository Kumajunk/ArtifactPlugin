package io.github.itokagimaru.artifact.artifact.artifacts.data.effect;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EffectStack {
    public static List<Effect> effects = new ArrayList<>();

    public static void addEffect(Effect effect){
        effects.add(effect);
    }

    public static List<Effect> getByTrigger(TriggerType.triggerType triggerType){
        return (List<Effect>) effects.stream().filter(effect -> effect.getTriggerType() == triggerType).toList();
    }

    public static void runByTrigger(TriggerType.triggerType triggerType, UUID playerUUID){
        for (Effect effect : getByTrigger(triggerType)){
            effect.run(playerUUID);
        }
    }

}
