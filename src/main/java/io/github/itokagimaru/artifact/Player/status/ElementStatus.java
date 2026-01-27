package io.github.itokagimaru.artifact.Player.status;

import java.util.Arrays;

public class ElementStatus {
    public enum Element {
        FIRE,
        WATER,
        NATURE,
        NULL
    }
    Element element = Element.NULL;

    public void setElement(Element element) {
        this.element = element;
    }
    public Element getElement() {
        return element;
    }
}
