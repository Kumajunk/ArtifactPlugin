package io.github.itokagimaru.artifact.Player.Status;

import java.util.*;

public class ModifierStack {

    private final Map<UUID, StatusModifier> modifiers = new HashMap<>();

    public void add(StatusModifier modifier) {
        modifiers.put(modifier.getId(), modifier);
    }

    public boolean remove(UUID id) {
        return modifiers.remove(id) != null;
    }

    public Collection<StatusModifier> getAll() {
        return modifiers.values();
    }

    public List<StatusModifier> getByStat(PlayerStatus.status stat) {
        return modifiers.values().stream()
                .filter(m -> m.getStat() == stat)
                .toList();
    }

    public void removeBySource(StatusModifier.EffectSource source) {
        modifiers.values().removeIf(m -> m.getSource() == source);
    }

    public Optional<StatusModifier> findById(UUID id) {
        return Optional.ofNullable(modifiers.get(id));
    }
}

