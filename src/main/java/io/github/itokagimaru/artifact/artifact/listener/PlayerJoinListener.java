package io.github.itokagimaru.artifact.artifact.listener;

import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import io.github.itokagimaru.artifact.Player.status.HpStatUpdater;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffectUpdater;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffectUpdater;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static io.github.itokagimaru.artifact.artifact.EquipPdc.loadFromPdc;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        PlayerStatusManager.addPlayerStatus(player.getUniqueId(), new PlayerStatus());
        for (Slot.artifactSlot slot : Slot.artifactSlot.values()) {
            BaseArtifact artifact = loadFromPdc(player, slot);
            if (artifact == null) continue;
            MainEffectUpdater.mainEffectUpdater(player, artifact);
            SubEffectUpdater.subEffectUpdater(player, artifact);
        }
        EffectStack.runByTrigger(TriggerType.triggerType.ON_UPDATE, player.getUniqueId());
        HpStatUpdater.hpStatUpdater(player);

    }
}
