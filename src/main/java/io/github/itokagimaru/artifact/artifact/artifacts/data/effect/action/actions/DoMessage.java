package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class DoMessage extends Action {
    List<Component> messages;

    public DoMessage(List<Component> messages) {
        this.messages = messages;
    }

    @Override
    public void run(UUID playerUuid){
        for (Component message : messages){
            Bukkit.getPlayer(playerUuid).sendMessage(message);
        }
    }
}
