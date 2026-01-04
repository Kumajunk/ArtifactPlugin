package io.github.itokagimaru.artifact.data;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class IntKey extends UsefulKey<Integer> {
    public IntKey(NamespacedKey key, Supplier<Integer> defaultValue) {
        super(key, PersistentDataType.INTEGER, defaultValue);
    }

    @Override
    public Integer get(PersistentDataContainerView dataContainerView) {
        if(dataContainerView.has(key, PersistentDataType.STRING)) {
            try {
                String rawInt = dataContainerView.get(key, PersistentDataType.STRING);
                if(rawInt == null) return defaultValue.get();
                return Integer.parseInt(rawInt);
            } catch (NumberFormatException e) {
                return defaultValue.get();
            }
        }
        return super.get(dataContainerView);
    }
}
