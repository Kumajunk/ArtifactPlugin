package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.action.actions;

import io.github.itokagimaru.artifact.Player.status.ElementStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatus;
import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;

import java.util.UUID;

public class DoSetElement extends Action{
    ElementStatus.Element element;
    String setType;
    public DoSetElement(ElementStatus.Element element, String setType){
        this.element = element;
        this.setType = setType;
    }

    @Override
    public void run(UUID playerUuid){
        PlayerStatus playerStatus = PlayerStatusManager.getPlayerStatus(playerUuid);
        playerStatus.setBaseElement(element);
    }
}
