package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.trigger;

import io.github.itokagimaru.artifact.Player.status.PlayerStatus;

public class Condition {
    public static boolean BLOW_HP(double triggerThreshold, PlayerStatus status){

        return triggerThreshold > 0;
    }
}
