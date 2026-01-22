package io.github.itokagimaru.artifact.Player.status;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HpStatUpdater {
    public static void hpStatUpdater(Player player){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(player.getUniqueId());
        if (playerStatus == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            player.sendMessage(Component.text(playerStatus.getStatus(PlayerStatus.playerStatus.HP)));
            attr.setBaseValue(playerStatus.getStatus(PlayerStatus.playerStatus.HP));
            player.setHealth(Math.min(player.getHealth(), attr.getValue()));
        }
    }
}
