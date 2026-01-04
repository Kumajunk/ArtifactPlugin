package io.github.itokagimaru.artifact.Player.Status;

import java.util.UUID;

public class StatusModifier {
    public enum ValueType{
        ADD,
        MULTIPLY
    }

    public enum EffectSource{
        SET_EFFECT,
        MAIN_EFFECT,
        SUB_EFFECT,
        WEAPON_EFFECT
    }

    UUID id;
    PlayerStatus.status stat;
    ValueType type; // ADD, MULTIPLY
    double value;
    EffectSource source;
    public StatusModifier(UUID id,
                          PlayerStatus.status stat,
                          ValueType type,
                          double value,
                          EffectSource source) {

        this.id = id;
        this.stat = stat;
        this.type = type;
        this.value = value;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public PlayerStatus.status getStat() {
        return stat;
    }

    public ValueType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public EffectSource getSource() {
        return source;
    }

}
