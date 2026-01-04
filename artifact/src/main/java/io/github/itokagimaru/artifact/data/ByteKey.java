package io.github.itokagimaru.artifact.data;


import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class ByteKey  extends UsefulKey<Byte>{
    public ByteKey(NamespacedKey key, Supplier<Byte> defaultValue) {
        super(key, PersistentDataType.BYTE,defaultValue);
    }
}
