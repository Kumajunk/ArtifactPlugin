package io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect;

import io.github.itokagimaru.artifact.Player.status.EffectSource;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.Player.status.StatusModifier;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubEffectUpdater {
    public static void subEffectUpdater(Player player, BaseArtifact artifact) {
        if (artifact == null) return;
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(player.getUniqueId());
        for (int i = 0; i < artifact.getSubEffects().length; i++) {
            if (artifact.getSubEffects()[i] == null) continue;
            playerStatus.addModifier(new StatusModifier(UUID.randomUUID(), subEffectToPlayerStatus(artifact.getSubEffects()[i]), StatusModifier.ValueType.MULTIPLY, artifact.getSubEffectsValue()[i], new EffectSource(EffectSource.EffectSourceType.SUB_EFFECT, artifact.getSeries().getInternalName())));
        }
        PlayerStatusManager.addPlayerStatus(player.getUniqueId(), playerStatus);
    }

    public static PlayerStatus.playerStatus subEffectToPlayerStatus(SubEffect.artifactSubEffect subEffect) {
        switch (subEffect) {
            case HP -> {
                return PlayerStatus.playerStatus.HP;
            }
            case ATK -> {
                return PlayerStatus.playerStatus.ATK;
            }
            case DEF -> {
                return PlayerStatus.playerStatus.DEF;
            }
            case VIT -> {
                return PlayerStatus.playerStatus.VIT;
            }
            case CRI -> {
                return PlayerStatus.playerStatus.CRI;
            }
            case FIRE_DMG_REDUCE -> {
                return PlayerStatus.playerStatus.FIRE_DMG_REDUCE;
            }
            case WATER_DMG_REDUCE -> {
                return PlayerStatus.playerStatus.WATER_DMG_REDUCE;
            }
            case NATURE_DMG_REDUCE -> {
                return PlayerStatus.playerStatus.NATURE_DMG_REDUCE;
            }
        }
        return null;
    }
}
