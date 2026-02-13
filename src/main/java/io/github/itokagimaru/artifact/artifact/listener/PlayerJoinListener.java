package io.github.itokagimaru.artifact.artifact.listener;

import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.Player.status.HpStatUpdater;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger.TriggerType;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffectUpdater;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffectUpdater;
import io.github.itokagimaru.artifact.data.ItemData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static io.github.itokagimaru.artifact.artifact.EquipPdc.loadFromPdc;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        ArtifactMain.updatePlayerArtifacts(e.getPlayer());
    }
}
