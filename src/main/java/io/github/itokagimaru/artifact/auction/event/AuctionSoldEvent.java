package io.github.itokagimaru.artifact.auction.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AuctionSoldEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID sellerId;
    private final String artifactName;
    private final double soldPrice;

    public AuctionSoldEvent(UUID sellerId, String artifactName, double soldPrice) {
        this.sellerId = sellerId;
        this.artifactName = artifactName;
        this.soldPrice = soldPrice;
    }

    public UUID getSellerId() { return sellerId; }
    public String getArtifactName() { return artifactName; }
    public double getSoldPrice() { return soldPrice; }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
