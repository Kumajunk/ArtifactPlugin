package io.github.itokagimaru.artifact.Player.status;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class AGIStatUpdater {
    public static void agiStatUpdater(Player player) {
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(player.getUniqueId());
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(playerStatus.getStatus(PlayerStatus.playerStatus.AGI));
        }
    }
}
