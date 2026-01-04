package io.github.itokagimaru.artifact.data;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class IntArrayKey extends UsefulKey<int[]> {
    public IntArrayKey(NamespacedKey key, Supplier<int[]> defaultValue) {
        super(key, PersistentDataType.INTEGER_ARRAY, defaultValue);
    }
}
