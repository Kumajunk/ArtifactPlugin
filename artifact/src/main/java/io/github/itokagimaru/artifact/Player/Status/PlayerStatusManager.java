package io.github.itokagimaru.artifact.Player.Status;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStatusManager {
    public static HashMap<UUID, PlayerStatus> playersStatus = new HashMap<>();

    public static void addPlayerStatus(UUID uuid, PlayerStatus playerStatus){
        playersStatus.put(uuid, playerStatus);
    }

    public static PlayerStatus getPlayerStatus(UUID uuid){
        PlayerStatus playerStatus = playersStatus.get(uuid);
        if (playerStatus != null) return playerStatus;
        playerStatus = new PlayerStatus();
        addPlayerStatus(uuid, playerStatus);
        return playerStatus;
    }
}
