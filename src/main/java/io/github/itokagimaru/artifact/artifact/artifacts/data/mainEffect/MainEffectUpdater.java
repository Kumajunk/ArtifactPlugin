package io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect;

import io.github.itokagimaru.artifact.Player.status.*;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MainEffectUpdater {
    public static void mainEffectUpdater(Player player, BaseArtifact artifact) {
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(player.getUniqueId());
        if (playerStatus == null) return;
        playerStatus.addModifier(new StatusModifier(UUID.randomUUID(), mainEffectToPlayerStatus(artifact.getMainEffect()), mainEffectValueType(artifact.getMainEffect()), artifact.getMainEffectValue(), new EffectSource(EffectSource.EffectSourceType.MAIN_EFFECT, artifact.getSeries().getInternalName())));
        PlayerStatusManager.addPlayerStatus(player.getUniqueId(), playerStatus);
    }

    public static PlayerStatus.playerStatus mainEffectToPlayerStatus(MainEffect.artifactMainEffect mainEffect) {
        switch (mainEffect) {
            case HP -> {
                return PlayerStatus.playerStatus.HP;
            }
            case ATK -> {
                return PlayerStatus.playerStatus.ATK;
            }
            case DEF -> {
                return PlayerStatus.playerStatus.DEF;
            }
            case LUK -> {
                return PlayerStatus.playerStatus.LUK;
            }
            case VIT -> {
                return PlayerStatus.playerStatus.VIT;
            }
            case CRI -> {
                return PlayerStatus.playerStatus.CRI;
            }
            case FIRE_DMG_BONUS -> {
                return PlayerStatus.playerStatus.FIRE_DMG_BONUS;
            }
            case WATER_DMG_BONUS -> {
                return PlayerStatus.playerStatus.WATER_DMG_BONUS;
            }
            case NATURE_DMG_BONUS -> {
                return PlayerStatus.playerStatus.NATURE_DMG_BONUS;
            }
        }
        return null;
    }

    private static StatusModifier.ValueType mainEffectValueType(MainEffect.artifactMainEffect mainEffect) {
        switch (mainEffect.getAddType) {
            case ADD ->  {
                return StatusModifier.ValueType.ADD;
            }
            case MULTIPLY ->  {
                return StatusModifier.ValueType.MULTIPLY;
            }
        }
        return null;
    }
}
