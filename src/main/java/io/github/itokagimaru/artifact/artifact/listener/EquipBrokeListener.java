package io.github.itokagimaru.artifact.artifact.listener;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.EquipPdc;
import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.event.EquipBrokeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class EquipBrokeListener implements Listener {

    @EventHandler
    public void onEquipBroke(EquipBrokeEvent event) {
        Player player = event.getPlayer();
        BaseArtifact artifact = event.getArtifact();

        // 1. 装備の強制解除
        EquipPdc.removeFromPdc(player, event.getSlot());

        // 2. アイテム化し、Stashへ送る
        String jsonArtifact = JsonConverter.serializeArtifact(artifact);
        ArtifactMain.getStashManager().giveOrStash(player.getUniqueId(), jsonArtifact, "耐久値が0になったため装備解除");

        // 3. プレイヤーへの通知
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
        
        Component title = Component.text("アーティファクト破損!").color(NamedTextColor.RED);
        Component subtitle = Component.text(event.getSlot().getSlotName + "の耐久値が0になり装備が外れました").color(NamedTextColor.GRAY);
        Title displayTitle = Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000)));
        
        player.showTitle(displayTitle);
        player.sendMessage(Component.text("装備していた ").color(NamedTextColor.RED)
                .append(Component.text(artifact.getSeries().getSeriesName()).color(NamedTextColor.GOLD))
                .append(Component.text(" (" + event.getSlot().getSlotName + ") の耐久値が0になったため、強制的に解除されました。").color(NamedTextColor.RED)));

        // 4. ステータスの再計算 (装備が外れたので更新する)
        ArtifactMain.updatePlayerArtifacts(player);
    }
}
