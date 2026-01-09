package io.github.itokagimaru.artifact.artifact.artifacts.data.effect;

import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;

import java.util.ArrayList;
import java.util.List;

public class EffectStack {
    public static List<Effect> effects = new ArrayList<>();

    public static void addEffect(Effect effect){
        effects.add(effect);
    }

    public static Effect[] getByTrigger(TriggerType.triggerType triggerType){
        return (Effect[]) effects.stream().filter(effect -> effect.getTriggerType() == triggerType).toArray();
    }

}
