package io.github.itokagimaru.artifact.auction.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.Result;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 自分の出品一覧を表示・管理するGUI
 */
public class AuctionMyListingsMenu extends BaseGui {

    private final AuctionManager manager;
    private final Gson gson = new Gson();

    public AuctionMyListingsMenu(AuctionManager manager) {
        super(54, "§d§l自分の出品");
        this.manager = manager;
    }

    /**
     * プレイヤー用にメニューをセットアップして開く
     */
    @Override
    public void open(Player player) {
        setupMenu(player);
        super.open(player);
    }

    private void setupMenu(Player player) {
        // 背景
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // プレイヤーの出品を取得
        List<AuctionListing> myListings = manager.getPlayerListings(player.getUniqueId());

        // 出品アイテムを表示
        for (int i = 0; i < myListings.size() && i < 45; i++) {
            AuctionListing listing = myListings.get(i);
            setItem(i, createListingItem(listing), p -> {
                new MyListingDetailMenu(manager, listing).open(p);
            });
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
            .setClickAction(ClickType.LEFT, p -> {
                new AuctionMainMenu(manager).open(p);
            }));
    }

    private ItemStack createListingItem(AuctionListing listing) {
        JsonObject json = gson.fromJson(listing.getArtifactData(), JsonObject.class);
        
        int slotId = json.has("slotId") ? json.get("slotId").getAsInt() : 0;
        Material material = getMaterialForSlot(slotId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String seriesName = json.has("seriesName") ? json.get("seriesName").getAsString() : "Unknown";
            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            String slotName = slot != null ? slot.getSlotName : "Unknown";
            
            meta.displayName(Utils.parseLegacy("§6" + seriesName + " §7- §e" + slotName));
            
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            
            // レベル
            int level = json.has("level") ? json.get("level").getAsInt() : 0;
            lore.add(Utils.parseLegacy("§7Lv.§f" + level));
            
            // Main効果
            int mainEffectId = json.has("mainEffectId") ? json.get("mainEffectId").getAsInt() : 0;
            int mainEffectValue = json.has("mainEffectValue") ? json.get("mainEffectValue").getAsInt() : 0;
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);
            String mainEffectName = mainEffect != null ? mainEffect.getText : "Unknown";
            lore.add(Utils.parseLegacy("§6Main: §f" + mainEffectName + " §a+" + formatPercent(mainEffectValue)));
            
            lore.add(Utils.parseLegacy(""));
            lore.add(Utils.parseLegacy("§8━━━━━━━━━━━━━━"));
            
            // 価格
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
                lore.add(Utils.parseLegacy(""));
                lore.add(Utils.parseLegacy("§aキャンセル可能"));
            } else {
                lore.add(Utils.parseLegacy(""));
                lore.add(Utils.parseLegacy("§cキャンセル不可"));
            }
            
            lore.add(Utils.parseLegacy(""));
            lore.add(Utils.parseLegacy("§eクリックで詳細"));
            
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private Material getMaterialForSlot(int slotId) {
        return switch (slotId) {
            case 0 -> Material.AMETHYST_SHARD;
            case 1 -> Material.PRISMARINE_SHARD;
            case 2 -> Material.DIAMOND;
            case 3 -> Material.EMERALD;
            case 4 -> Material.LAPIS_LAZULI;
            case 5 -> Material.QUARTZ;
            default -> Material.NETHER_STAR;
        };
    }

    private String formatPercent(int value) {
        return String.format("%.1f%%", value / 10.0);
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
                    .setClickAction(ClickType.LEFT, player -> {
                        Result<Void> result = manager.cancelListing(player, listing.getListingId());
                        if (result.isSuccess()) {
                            player.sendMessage("§a出品をキャンセルしました");
                            new AuctionMyListingsMenu(manager).open(player);
                        } else {
                            player.sendMessage("§c" + result.getErrorMessage());
                        }
                    }));
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
                .setClickAction(ClickType.LEFT, player -> {
                    new AuctionMyListingsMenu(manager).open(player);
                }));
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
