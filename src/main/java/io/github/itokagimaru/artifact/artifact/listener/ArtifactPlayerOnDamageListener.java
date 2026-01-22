package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.data.EntityData;
import net.kyori.adventure.text.Component;
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
        double attackCharge = 1.0;
        if (damager instanceof Player attackerPlayer) {
            attackCharge = attackerPlayer.getCooledAttackStrength(0f);
            if (attackCharge <= 0.98) return;
            EffectStack.runByTrigger(TriggerType.triggerType.ON_ATTACK, attackerPlayer.getUniqueId());
            PlayerStatus attackerStatus = PlayerStatusManager.getPlayerStatus(attackerPlayer.getUniqueId());
            attackerATK = attackerStatus.getStatus(PlayerStatus.playerStatus.ATK);

            Random rand = new Random();
            if (rand.nextDouble() < attackerStatus.getStatus(PlayerStatus.playerStatus.CRI)){
                EffectStack.runByTrigger(TriggerType.triggerType.ON_CRITICAL_HIT, attackerPlayer.getUniqueId());
                attackerPlayer.sendMessage(Component.text("You have critical hit."));
                criDMGBonus += attackerStatus.getStatus(PlayerStatus.playerStatus.CRIDMG);
            }
        } else {
            attackerATK = EntityData.ENEMY_ATK.get(damager);
        }

        if (damaged instanceof Player damagerPlayer){
            EffectStack.runByTrigger(TriggerType.triggerType.ON_DAMAGE, damagerPlayer.getUniqueId());
            PlayerStatus defenderStatus = PlayerStatusManager.getPlayerStatus(damagerPlayer.getUniqueId());
            defenderDEF = defenderStatus.getStatus(PlayerStatus.playerStatus.DEF);
        } else {
            defenderDEF = EntityData.ENEMY_DEF.get(damaged);
        }
        baseDmg = (attackerATK/2) - (defenderDEF/4);
        baseDmg = baseDmg*criDMGBonus;
        if (damager instanceof Player attackerPlayer){
            attackerPlayer.sendMessage(Component.text(baseDmg));
        }
        if (baseDmg < 0) baseDmg = 0;
        event.setDamage(baseDmg);

    }


}
