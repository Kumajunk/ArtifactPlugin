package io.github.itokagimaru.artifact.Player.status;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HpStatUpdater {
    public static void hpStatUpdater(Player player){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(player.getUniqueId());
        if (playerStatus == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(playerStatus.getStatus(PlayerStatus.playerStatus.HP));
        }
    }
}
