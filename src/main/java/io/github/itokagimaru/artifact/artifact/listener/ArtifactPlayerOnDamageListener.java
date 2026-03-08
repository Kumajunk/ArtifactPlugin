package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.Player.status.ElementStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.data.EntityData;
import io.github.itokagimaru.artifact.utils.ByteArrayConverter;
import io.github.itokagimaru.artifact.utils.DamageActionBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArtifactPlayerOnDamageListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();
        double damageRate = (1 + event.getFinalDamage() / 20);
        double attackerATK;
        ElementStatus.Element attackerElement;
        double criDMGBonus = 1.0;
        boolean criFlag = false;
        double defenderDEF;
        double baseDmg;
        double defenderReduceRate = 0.0;
        double attackerDamageBonusRate = 1.0;
        NamedTextColor damageColor = NamedTextColor.WHITE;
        if(damager instanceof Projectile projectile) {
            ProjectileSource projectileSource = projectile.getShooter();
            if(projectileSource instanceof Entity source) {
                // ArtifactMain.getInstance().testLog(source.toString());
                damager = source;
            }else {
                ArtifactMain.getInstance().testLog("ダメージソースの取得に失敗");
            }
        }
        if (damager instanceof Player attackerPlayer) { //攻撃側の処理
            if (event.getDamageSource().getDamageType() == DamageType.PLAYER_ATTACK) {
                event.setCancelled(true);
                return;
            }
            EffectStack.runByTrigger(TriggerType.triggerType.ON_ATTACK, attackerPlayer.getUniqueId());
            PlayerStatus attackerStatus = PlayerStatusManager.getPlayerStatus(attackerPlayer.getUniqueId());
            attackerATK = attackerStatus.getStatus(PlayerStatus.playerStatus.ATK);
            if (attackerPlayer.getInventory().getItemInMainHand().getType() == Material.AIR ) attackerATK /= 2;
            Random rand = new Random();
            if (rand.nextDouble() < attackerStatus.getStatus(PlayerStatus.playerStatus.CRI)){
                EffectStack.runByTrigger(TriggerType.triggerType.ON_CRITICAL_HIT, attackerPlayer.getUniqueId());
                criFlag = true;
                criDMGBonus += attackerStatus.getStatus(PlayerStatus.playerStatus.CRIDMG);
            }

            attackerElement = attackerStatus.getElement();

            switch (attackerElement) {
                case FIRE -> {
                    attackerDamageBonusRate += attackerStatus.getStatus(PlayerStatus.playerStatus.FIRE_DMG_BONUS);
                    damageColor = NamedTextColor.RED;
                }
                case WATER -> {
                    attackerDamageBonusRate += attackerStatus.getStatus(PlayerStatus.playerStatus.WATER_DMG_BONUS);
                    damageColor = NamedTextColor.BLUE;
                }
                case NATURE -> {
                    attackerDamageBonusRate += attackerStatus.getStatus(PlayerStatus.playerStatus.NATURE_DMG_BONUS);
                    damageColor = NamedTextColor.GREEN;
                }
            }
            Bukkit.getScheduler().runTask(ArtifactMain.getInstance(),() -> {
                    attackerStatus.setWeaponElement(ElementStatus.Element.NULL);
            });


            //ノックバックの計算
            Vector diff = damaged.getLocation().toVector().subtract(attackerPlayer.getLocation().toVector());
            if (diff.lengthSquared() != 0) {
                Vector direction = diff.normalize();
                double power = Math.max(0.2, 1.0) * 1.025;
                Vector knockback = direction.multiply(power).setY(0.3);
                //素のノックバック体制の保存
                AttributeInstance attr;
                if (damaged instanceof Player damagedPlayer) attr = damagedPlayer.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                else if (damaged instanceof LivingEntity damagedLivingEntity) attr = damagedLivingEntity.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                else if (damaged instanceof Monster damagedMonster) attr = damagedMonster.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                else attr = null;
                if (attr != null) {
                    double baseKnockbackResistance = attr.getBaseValue();
                    //ノックバックを一旦無効化
                    attr.setBaseValue(1.0);
                    //1tick後にカスタムノックバックを適応
                    Bukkit.getScheduler().runTask(ArtifactMain.getInstance(),() ->{
                        attr.setBaseValue(baseKnockbackResistance);
                        damaged.setVelocity(knockback.multiply(1-baseKnockbackResistance));
                    });
                }
            }

        } else {
            attackerATK = ByteArrayConverter.ByteToDouble(EntityData.ENEMY_ATK.get(damager));
            String enemyElement = EntityData.ENEMY_ELEMENT.get(damager);
            switch (enemyElement){
                case "fire" -> {
                    attackerElement = ElementStatus.Element.FIRE;
                    attackerDamageBonusRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_FIRE_DMG_BONUS.get(damaged));
                    damageColor = NamedTextColor.RED;
                }
                case "water" -> {
                    attackerElement = ElementStatus.Element.WATER;
                    attackerDamageBonusRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_WATER_DMG_BONUS.get(damaged));
                    damageColor = NamedTextColor.BLUE;
                }
                case "nature" -> {
                    attackerElement = ElementStatus.Element.NATURE;
                    attackerDamageBonusRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_NATURE_DMG_BONUS.get(damaged));
                    damageColor = NamedTextColor.GREEN;
                }
                default -> attackerElement = ElementStatus.Element.NULL;
            }
        }

        if (damaged instanceof Player damagedPlayer){ //被攻撃側の処理
            EffectStack.runByTrigger(TriggerType.triggerType.ON_DAMAGE, damagedPlayer.getUniqueId());
            PlayerStatus defenderStatus = PlayerStatusManager.getPlayerStatus(damagedPlayer.getUniqueId());
            defenderDEF = defenderStatus.getStatus(PlayerStatus.playerStatus.DEF);
            switch (attackerElement) {
                case FIRE -> defenderReduceRate += defenderStatus.getStatus(PlayerStatus.playerStatus.FIRE_DMG_REDUCE);
                case WATER -> defenderReduceRate += defenderStatus.getStatus(PlayerStatus.playerStatus.WATER_DMG_REDUCE);
                case NATURE -> defenderReduceRate += defenderStatus.getStatus(PlayerStatus.playerStatus.NATURE_DMG_REDUCE);
                default -> defenderReduceRate += 0;
            }
        } else {
            defenderDEF = ByteArrayConverter.ByteToDouble(EntityData.ENEMY_DEF.get(damaged));
            switch (attackerElement) {
                case FIRE -> defenderReduceRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_FIRE_DMG_REDUCE.get(damaged));
                case WATER -> defenderReduceRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_WATER_DMG_REDUCE.get(damaged));
                case NATURE -> defenderReduceRate += ByteArrayConverter.ByteToDouble(EntityData.ENEMY_NATURE_DMG_REDUCE.get(damaged));
                default -> defenderReduceRate += 0;
            }
        }
        if (defenderReduceRate >= 1) defenderReduceRate = 1;

        baseDmg = (attackerATK/2) - (defenderDEF/4);
        baseDmg = baseDmg*criDMGBonus;
        if (baseDmg < 0) baseDmg = 0;
        double finalDmg = baseDmg*damageRate*attackerDamageBonusRate*(1-defenderReduceRate);
        if (damager instanceof Player attackerPlayer) {
            attackerPlayer.sendActionBar(DamageActionBarUtil.buildCenteredDamageBar(attackerPlayer, damaged, doubleToString(finalDmg), damageColor, criFlag));
        }
        if (damaged instanceof Player defenderPlayer) {
            defenderPlayer.sendActionBar(DamageActionBarUtil.buildCenteredDamageBar(damager, defenderPlayer, doubleToString(finalDmg), damageColor, criFlag));
        }
        event.setDamage(finalDmg);
    }
    private String doubleToString(double d){
        return String.format("%.2f", d);
    }


}
