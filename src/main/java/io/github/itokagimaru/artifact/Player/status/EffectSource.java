package io.github.itokagimaru.artifact.Player.status;

public class EffectSource {
    public enum EffectSourceType {
        SET_EFFECT,
        MAIN_EFFECT,
        SUB_EFFECT,
        WEAPON_EFFECT,
        Skill
    }

    private final EffectSourceType effectSourceType;
    private final String id;

    public EffectSource(EffectSourceType effectSourceType, String id){
        this.effectSourceType = effectSourceType;
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public EffectSourceType getType(){
        return effectSourceType;
    }
}
