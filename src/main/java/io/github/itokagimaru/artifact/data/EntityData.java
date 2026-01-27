package io.github.itokagimaru.artifact.data;

import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import org.bukkit.NamespacedKey;

public class EntityData {
    private static final String NAMESPACE = "artifact";

    private static NamespacedKey getKey(String key) {
        return new NamespacedKey(NAMESPACE, key);
    }
    public static final IntKey PHASE = new IntKey(getKey("phase"), () -> 0);
    public static final ByteArrayKey ENEMY_ATK = new ByteArrayKey(getKey("atk"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_DEF = new ByteArrayKey(getKey("def"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_FIRE_DMG_REDUCE = new ByteArrayKey(getKey("f_reduce"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_WATER_DMG_REDUCE = new ByteArrayKey(getKey("w_reduce"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_NATURE_DMG_REDUCE = new ByteArrayKey(getKey("n_reduce"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_FIRE_DMG_BONUS = new ByteArrayKey(getKey("f_bonus"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_WATER_DMG_BONUS = new ByteArrayKey(getKey("w_bonus"), () -> ByteArrayConverter.toByte(0.0));
    public static final ByteArrayKey ENEMY_NATURE_DMG_BONUS = new ByteArrayKey(getKey("n_bonus"), () -> ByteArrayConverter.toByte(0.0));
    public static final StringKey ENEMY_ELEMENT = new StringKey(getKey("element"), () -> "");

}
