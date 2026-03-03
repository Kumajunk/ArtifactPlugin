package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static io.github.itokagimaru.artifact.utils.Utils.sync;

/**
 * 自分の出品一覧を表示・管理するGUI
 */
public class AuctionMyListingsMenu extends BaseGui {

    private final AuctionManager manager;

    public AuctionMyListingsMenu(AuctionManager manager) {
        super(54, "§d§l自分の出品");
        this.manager = manager;
    }

    /**
     * プレイヤー用にメニューをセットアップして開く
     */
    @Override
    public void open(Player player) {
        manager.getPlayerListings(player.getUniqueId())
                .thenAccept(myListings -> sync(() -> {
                    setupMenu(myListings);
                    openGui(player, this);
                }))
                .exceptionally(ex -> {
                    sync(() -> player.sendMessage("§c出品情報の読み込みに失敗しました"));
                    return null;
                });
    }

    private void setupMenu(List<AuctionListing> myListings) {
        // 背景
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // プレイヤーの出品物を表示
        for (int i = 0; i < myListings.size() && i < 45; i++) {
            AuctionListing listing = myListings.get(i);
            setItem(i, createListingItem(listing), p -> new MyListingDetailMenu(manager, listing).open(p));
        }

        // 情報
        setItem(49, new ItemBuilder()
            .setMaterial(Material.PAPER)
            .setName("§e§l出品状況")
            .addLore("§f現在の出品数: §a" + myListings.size() + "§7/§f10")
            .addLore("")
            .addLore("§7アイテムをクリックで詳細・キャンセル"));

        // 戻るボタン
        setItem(53, new ItemBuilder()
            .setMaterial(Material.ARROW)
            .setName("§7§l戻る")
            .setClickAction(ClickType.LEFT, p -> new AuctionMainMenu(manager).open(p)));
    }

    private ItemStack createListingItem(AuctionListing listing) {
        BaseArtifact artifact = JsonConverter.deserializeArtifact(listing.getArtifactData());
        if (artifact != null) {
            ItemStack item = ArtifactToItem.convert(artifact);
            appendMyListingLore(item, listing);
            return item;
        }

        ItemStack fallback = new ItemStack(Material.BARRIER);
        fallback.editMeta(meta ->
                meta.displayName(Component.text("データ復元失敗")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));
        return fallback;
    }

    private void appendMyListingLore(ItemStack item, AuctionListing listing) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.lore() != null ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();

        lore.add(Component.text("━━━━━ AUCTION ━━━━━").color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        String priceStr = formatPrice(listing.getDisplayPrice());
        if (listing.getType() == AuctionType.BIN) {
            lore.add(Utils.parseLegacy("§a即時購入: §f$" + priceStr));
        } else {
            if (listing.getCurrentBid() > 0) {
                lore.add(Utils.parseLegacy("§e現在入札額: §f$" + priceStr));
                lore.add(Utils.parseLegacy("§7入札者あり"));
            } else {
                lore.add(Utils.parseLegacy("§7開始価格: §f$" + priceStr));
                lore.add(Utils.parseLegacy("§8入札なし"));
            }
        }
        
        // 残り時間
        lore.add(Utils.parseLegacy("§7残り: §f" + formatTime(listing.getRemainingTime())));
        
        // キャンセル可能かどうか
        long cancelWindow = 24 * 60 * 60 * 1000; // 24時間
        if (listing.isCancellable(cancelWindow)) {
            lore.add(Component.empty());
            lore.add(Utils.parseLegacy("§aキャンセル可能"));
        } else {
            lore.add(Component.empty());
            lore.add(Utils.parseLegacy("§cキャンセル不可"));
        }
        
        lore.add(Component.empty());
        lore.add(Utils.parseLegacy("§eクリックで詳細"));

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private String formatPrice(long price) {
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }

    private String formatTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        
        if (hours > 24) {
            long days = hours / 24;
            hours = hours % 24;
            return days + "日 " + hours + "時間";
        }
        return hours + "時間 " + minutes + "分";
    }

    /**
     * 自分の出品詳細画面（キャンセル機能付き）
     */
    private static class MyListingDetailMenu extends BaseGui {
        private final AuctionManager manager;
        private final AuctionListing listing;

        public MyListingDetailMenu(AuctionManager manager, AuctionListing listing) {
            super(27, "§d出品管理");
            this.manager = manager;
            this.listing = listing;
            setupMenu();
        }

        private void setupMenu() {
            fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

            // 情報表示
            setItem(13, new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setName("§e§l出品情報")
                .addLore("§f種別: §e" + listing.getType().getDisplayName())
                .addLore("§f価格: §a$" + NumberFormat.getNumberInstance(Locale.US).format(listing.getDisplayPrice()))
                .addLore("§f残り時間: §b" + formatTime(listing.getRemainingTime())));

            // キャンセルボタン
            long cancelWindow = 24 * 60 * 60 * 1000;
            boolean canCancel = listing.isCancellable(cancelWindow);
            boolean hasBid = listing.getCurrentBidderId() != null;

            if (canCancel && !hasBid) {
                setItem(11, new ItemBuilder()
                    .setMaterial(Material.RED_WOOL)
                    .setName("§c§lキャンセル")
                    .addLore("§7出品をキャンセルします")
                    .addLore("§7アイテムは返却されます")
                    .addLore("")
                    .addLore("§eクリックでキャンセル")
                    .setClickAction(ClickType.LEFT, player -> manager.cancelListing(player, listing.getListingId())
                            .thenAccept(result -> sync(() -> {
                                if (result.isSuccess()) {
                                    player.sendMessage("§a出品をキャンセルしました");
                                    new AuctionMyListingsMenu(manager).open(player);
                                } else {
                                    player.sendMessage("§c" + result.getErrorMessage());
                                }
                            }))
                            .exceptionally(ex -> {
                                sync(() -> player.sendMessage("§cキャンセル処理に失敗しました"));
                                return null;
                            })));
            } else {
                setItem(11, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c§lキャンセル不可")
                    .addLore(hasBid ? "§7入札があるためキャンセルできません" : "§7キャンセル可能期間を過ぎています"));
            }

            // 戻るボタン
            setItem(15, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§7§l戻る")
                .setClickAction(ClickType.LEFT, player -> new AuctionMyListingsMenu(manager).open(player)));
        }

        private String formatTime(long millis) {
            long hours = millis / (1000 * 60 * 60);
            if (hours > 24) {
                return (hours / 24) + "日";
            }
            return hours + "時間";
        }
    }
}
