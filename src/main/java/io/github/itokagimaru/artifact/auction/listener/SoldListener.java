package io.github.itokagimaru.artifact.auction.listener;

import io.github.itokagimaru.artifact.auction.event.AuctionSoldEvent;
import io.github.itokagimaru.artifact.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SoldListener implements Listener {
    @EventHandler
    public void onAuctionSold(AuctionSoldEvent e){
        Player seller = Bukkit.getPlayer(e.getSellerId());

        // 売り主がオンラインの場合のみメッセージを送る
        if (seller != null && seller.isOnline()) {
            String message = String.format("§6[Auction] §e%s §fが §a%.1f円 §fで売れました！",
                    e.getArtifactName(), e.getSoldPrice());

            seller.sendMessage(Utils.parseLegacy(message));
        }
    }
}
