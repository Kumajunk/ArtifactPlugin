package io.github.itokagimaru.artifact.data;

import org.bukkit.NamespacedKey;

public class ItemData {
    private static final String NAMESPACE = "artifact";

    /**
     * Get key instance for pdc container id
     *
     * @param key key
     * @return New instance of NamespacedKey
     */
    private static NamespacedKey getKey(String key) {
        return new NamespacedKey(NAMESPACE, key);
    }
    public static final ByteArrayKey MAIN_VALUE = new ByteArrayKey(getKey("main_val"), () -> new byte[8]);
    public static final ByteArrayKey SUB_VALUE = new ByteArrayKey(getKey("sub_val"), () -> new byte[32]);
    public static final ByteKey IS_SKILL_ITEM = new ByteKey(getKey("is_skill"), () -> (byte) 0);
    public static final IntKey TIER = new IntKey(getKey("tier"), () -> 0);
    public static final IntKey MAIN_ID = new IntKey(getKey("main_id"), () -> 0);
    public static final IntKey SERIES_ID = new IntKey(getKey("series_id"), () -> 0);
    public static final IntKey LV = new IntKey(getKey("lv"), () -> 0);
    public static final IntKey SLOT = new IntKey(getKey("slot_id"), () -> 0);
    public static final IntArrayKey SUB_ID = new IntArrayKey(getKey("sub_id"), () -> new int[4]);
    public static final StringKey UUID = new StringKey(getKey("uuid"), () -> "");
    public static final StringKey SKILL_KEY = new StringKey(getKey("skill_key"), () -> "");
}
