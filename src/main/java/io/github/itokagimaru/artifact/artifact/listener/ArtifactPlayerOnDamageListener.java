package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.data.EntityData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class ArtifactPlayerOnDamageListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();
        double attackerATK;
        double criDMGBonus = 1.0;
        double defenderDEF;
        double baseDmg;
        double defenderReduceRate = 0.0;
        if (damaged instanceof Player attackerPlayer) {

            PlayerStatus attackerStatus = PlayerStatusManager.getPlayerStatus(attackerPlayer.getUniqueId());
            attackerATK = attackerStatus.getStatus(PlayerStatus.playerStatus.ATK);
            Random rand = new Random();
            if (rand.nextDouble() < attackerStatus.getStatus(PlayerStatus.playerStatus.CRI)){
                criDMGBonus += attackerStatus.getStatus(PlayerStatus.playerStatus.CRIDMG);
            }
        } else {
            attackerATK = EntityData.ENEMY_ATK.get(damaged);
        }

        if (damager instanceof Player damagerPlayer){
            PlayerStatus defenderStatus = PlayerStatusManager.getPlayerStatus(damagerPlayer.getUniqueId());
            defenderDEF = defenderStatus.getStatus(PlayerStatus.playerStatus.DEF);
        } else {
            defenderDEF = EntityData.ENEMY_DEF.get(damager);
        }

        baseDmg = attackerATK * (defenderDEF / 1.0);


    }


}
