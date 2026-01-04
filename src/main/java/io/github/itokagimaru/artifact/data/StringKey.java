package io.github.itokagimaru.artifact.data;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class StringKey extends UsefulKey<String> {
    public StringKey(NamespacedKey key, Supplier<String> defaultValue) {
        super(key, PersistentDataType.STRING, defaultValue);
    }
}
