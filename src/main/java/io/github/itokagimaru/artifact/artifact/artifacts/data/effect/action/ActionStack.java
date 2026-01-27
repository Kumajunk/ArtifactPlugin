package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action;

import io.github.itokagimaru.artifact.Player.status.HpStatUpdater;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ActionStack {
    Action[] actions;

    public ActionStack(Action[] actions){
        this.actions = actions;
    }

    public void runActions(UUID playerUuid){
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;
        for (Action action : actions){
            action.run(playerUuid);
            HpStatUpdater.hpStatUpdater(player);
        }
    }
}
