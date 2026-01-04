package io.github.itokagimaru.artifact.Artifact.Artifacts.Data.Effect.Trigger;

import io.github.itokagimaru.artifact.Player.Status.PlayerStatus;

public class Condition {
    public static boolean BLOW_HP(double triggerThreshold, PlayerStatus status){

        return triggerThreshold > 0;
    }
}
