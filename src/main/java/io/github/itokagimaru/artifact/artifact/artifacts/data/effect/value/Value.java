package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value;

public class Value {
    String value;
    public Calculator.calculateType type;
    public Value(String value, Calculator.calculateType type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }
    public Calculator.calculateType getType() {
        return type;
    }
}
