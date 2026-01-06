package io.github.itokagimaru.artifact.auction.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.Result;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.NumberFormat;
import java.util.*;

/**
 * 出品詳細を表示するGUI
 */
public class AuctionDetailMenu extends BaseGui {

    private final AuctionManager manager;
    private final UUID listingId;
    private final Gson gson = new Gson();

    public AuctionDetailMenu(AuctionManager manager, UUID listingId) {
        super(54, "§6出品詳細");
        this.manager = manager;
        this.listingId = listingId;
        setupMenu();
    }

    private void setupMenu() {
        // 背景
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        Optional<AuctionListing> optListing = manager.getListingById(listingId);
        if (optListing.isEmpty()) {
            setItem(22, new ItemBuilder()
                .setMaterial(Material.BARRIER)
                .setName("§c出品が見つかりません")
                .addLore("§7この出品は終了または削除されました"));
            
            setItem(49, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§7§l戻る")
                .setClickAction(ClickType.LEFT, player -> {
                    new AuctionMainMenu(manager).open(player);
                }));
            return;
        }

        AuctionListing listing = optListing.get();
        JsonObject json = gson.fromJson(listing.getArtifactData(), JsonObject.class);

        // メインアイテム表示（中央）
        setItem(13, createDetailItem(listing, json), null);

        // 情報パネル
        createInfoPanel(listing, json);

        // 購入/入札ボタン
        if (listing.getType() == AuctionType.BIN) {
            setItem(38, new ItemBuilder()
                .setMaterial(Material.GOLD_INGOT)
                .setName("§a§l即時購入")
                .addLore("§f価格: §e$" + formatPrice(listing.getPrice()))
                .addLore("")
                .addLore("§eクリックで購入")
                .setClickAction(ClickType.LEFT, player -> {
                    handlePurchase(player, listing);
                }));
        } else {
            long minBid = listing.getCurrentBid() > 0 
                ? listing.getCurrentBid() + 100 
                : listing.getPrice();
            
            setItem(38, new ItemBuilder()
                .setMaterial(Material.EMERALD)
                .setName("§a§l入札する")
                .addLore("§f最低入札額: §e$" + formatPrice(minBid))
                .addLore("")
                .addLore("§eクリックして入札額を入力")
                .setClickAction(ClickType.LEFT, player -> {
                    handleBid(player, listing, minBid);
                }));
        }

        // 出品者情報
        String sellerName = Bukkit.getOfflinePlayer(listing.getSellerId()).getName();
        setItem(42, new ItemBuilder()
            .setMaterial(Material.PLAYER_HEAD)
            .setName("§d§l出品者")
            .addLore("§f" + (sellerName != null ? sellerName : "Unknown")));

        // 戻るボタン
        setItem(49, new ItemBuilder()
            .setMaterial(Material.ARROW)
            .setName("§7§l戻る")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionListingMenu(manager, null, 0).open(player);
            }));
    }

    private org.bukkit.inventory.ItemStack createDetailItem(AuctionListing listing, JsonObject json) {
        int slotId = json.has("slotId") ? json.get("slotId").getAsInt() : 0;
        Material material = getMaterialForSlot(slotId);
        
        var item = new org.bukkit.inventory.ItemStack(material);
        var meta = item.getItemMeta();
        
        if (meta != null) {
            String seriesName = json.has("seriesName") ? json.get("seriesName").getAsString() : "Unknown";
            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            String slotName = slot != null ? slot.getSlotName : "Unknown";
            
            meta.displayName(Utils.parseLegacy("§6§l" + seriesName + " §7- §e" + slotName));
            
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            
            // レベル
            int level = json.has("level") ? json.get("level").getAsInt() : 0;
            lore.add(Utils.parseLegacy("§7Lv.§f" + level + " §8/ 30"));
            lore.add(Utils.parseLegacy(""));
            
            // Main効果
            int mainEffectId = json.has("mainEffectId") ? json.get("mainEffectId").getAsInt() : 0;
            int mainEffectValue = json.has("mainEffectValue") ? json.get("mainEffectValue").getAsInt() : 0;
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);
            String mainEffectName = mainEffect != null ? mainEffect.getText : "Unknown";
            lore.add(Utils.parseLegacy("§6§lMain効果"));
            lore.add(Utils.parseLegacy("§f" + mainEffectName + " §a+" + formatPercent(mainEffectValue)));
            lore.add(Utils.parseLegacy(""));
            
            // Sub効果
            lore.add(Utils.parseLegacy("§e§lSub効果"));
            if (json.has("subEffectIds") && json.has("subEffectValues")) {
                String[] subIds = json.get("subEffectIds").getAsString().split(",");
                String[] subValues = json.get("subEffectValues").getAsString().split(",");
                
                for (int i = 0; i < subIds.length && i < subValues.length; i++) {
                    if (!subIds[i].isEmpty()) {
                        try {
                            int subId = Integer.parseInt(subIds[i].trim());
                            int subValue = Integer.parseInt(subValues[i].trim());
                            SubEffect.artifactSubEffect subEffect = SubEffect.artifactSubEffect.fromId(subId);
                            if (subEffect != null) {
                                lore.add(Utils.parseLegacy("§f" + subEffect.getText + " §a+" + formatPercent(subValue)));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void createInfoPanel(AuctionListing listing, JsonObject json) {
        // オークション種別
        setItem(29, new ItemBuilder()
            .setMaterial(listing.getType() == AuctionType.BIN ? Material.GOLD_BLOCK : Material.CLOCK)
            .setName("§d§l種別")
            .addLore("§f" + listing.getType().getDisplayName()));

        // 価格情報
        setItem(31, new ItemBuilder()
            .setMaterial(Material.SUNFLOWER)
            .setName("§e§l価格")
            .addLore(listing.getType() == AuctionType.BIN 
                ? "§f即時購入: §a$" + formatPrice(listing.getPrice())
                : listing.getCurrentBid() > 0 
                    ? "§f現在入札額: §a$" + formatPrice(listing.getCurrentBid())
                    : "§f開始価格: §7$" + formatPrice(listing.getPrice())));

        // 残り時間
        setItem(33, new ItemBuilder()
            .setMaterial(Material.CLOCK)
            .setName("§b§l残り時間")
            .addLore("§f" + formatTime(listing.getRemainingTime())));
    }

    private void handlePurchase(Player player, AuctionListing listing) {
        // 自己購入チェック
        if (listing.getSellerId().equals(player.getUniqueId())) {
            player.sendMessage("§c自分の出品は購入できません");
            return;
        }

        Result<Void> result = manager.purchaseBin(player, listing.getListingId());
        
        if (result.isSuccess()) {
            player.sendMessage("§a購入が完了しました!");
            player.closeInventory();
        } else {
            player.sendMessage("§c" + result.getErrorMessage());
        }
    }

    /**
     * 入札処理を行う
     */
    private void handleBid(Player player, AuctionListing listing, long minBid) {
        // 自己入札チェック
        if (listing.getSellerId().equals(player.getUniqueId())) {
            player.sendMessage("§c自分の出品には入札できません");
            return;
        }

        player.closeInventory();

        Utils.promptTextInput(
            player,
            "入札額を入力してください（最低: $" + formatPrice(minBid) + "）",
            15,
            input -> {
                try {
                    long bidAmount = Long.parseLong(input.replaceAll("[^0-9]", ""));

                    if (bidAmount < minBid) {
                        player.sendMessage("§c入札額は$" + formatPrice(minBid) + "以上である必要があります");
                        new AuctionDetailMenu(manager, listingId).open(player);
                        return;
                    }

                    Result<Void> result = manager.placeBid(player, listing.getListingId(), bidAmount);

                    if (result.isSuccess()) {
                        player.sendMessage("§a$" + formatPrice(bidAmount) + "で入札しました！");
                    } else {
                        player.sendMessage("§c" + result.getErrorMessage());
                    }

                    new AuctionDetailMenu(manager, listingId).open(player);

                } catch (NumberFormatException e) {
                    player.sendMessage("§c無効な金額です。数字を入力してください");
                    new AuctionDetailMenu(manager, listingId).open(player);
                }
            }
        );
    }

    // ユーティリティメソッド
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
}
