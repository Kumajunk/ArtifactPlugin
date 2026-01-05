package io.github.itokagimaru.artifact.Player.status;

import java.util.UUID;

public class StatusModifier {
    public enum ValueType{
        ADD,
        MULTIPLY
    }

    UUID id;
    PlayerStatus.playerStatus stat;
    ValueType type; // ADD, MULTIPLY
    double value;
    EffectSource source;
    public StatusModifier(UUID id,
                          PlayerStatus.playerStatus stat,
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

    public PlayerStatus.playerStatus getStat() {
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
