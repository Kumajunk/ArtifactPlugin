package io.github.itokagimaru.artifact.Player.status;

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

    public List<StatusModifier> getByStat(PlayerStatus.playerStatus stat) {
        return modifiers.values().stream().filter(m -> m.getStat() == stat).toList();
    }

    public List<StatusModifier> getByType(StatusModifier.ValueType type){
        return modifiers.values().stream().filter(m -> m.getType() == type).toList();
    }

    public void removeBySource(EffectSource.EffectSourceType type, String id) {
        modifiers.values().removeIf(m -> m.getSource().getType() == type & id.equals(m.getSource().getId()));
    }

    public void removeEachBySource(EffectSource.EffectSourceType type, String id) {
        remove(modifiers.values().stream().filter(m -> m.getSource().getType() == type & id.equals(m.getSource().getId())).toList().get(0).getId());
    }

    public StatusModifier findById(UUID id) {
        return modifiers.get(id);
    }
}

