package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.condition.Conditions;

import io.github.itokagimaru.artifact.artifact.listener.ItemUseListener;
import io.github.itokagimaru.artifact.data.ItemData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class IsUseSkill implements ConditionWithEvent{
    String key;
    public IsUseSkill(String key){
        this.key=key;
    }

    @Override
    public boolean isTrue(UUID playerUuid, Event event){
        if (event == null) return false;
        if (event instanceof PlayerInteractEvent interactEvent){
            ItemStack skillItem = interactEvent.getItem();
            return ItemData.IS_SKILL_ITEM.get(skillItem) != 0 && ItemData.SKILL_KEY.get(skillItem).equals(key);
        }
        return false;
    }
}
