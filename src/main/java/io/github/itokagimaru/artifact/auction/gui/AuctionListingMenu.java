package io.github.itokagimaru.artifact.auction.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.BaseGui;
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
 * オークション出品一覧を表示するGUI
 */
public class AuctionListingMenu extends BaseGui {

    private final AuctionManager manager;
    private final AuctionSearchFilter filter;
    private final int page;
    private static final int ITEMS_PER_PAGE = 45;
    private final Gson gson = new Gson();

    public AuctionListingMenu(AuctionManager manager, AuctionSearchFilter filter, int page) {
        super(54, "§6出品一覧 - ページ " + (page + 1));
        this.manager = manager;
        this.filter = filter != null ? filter : new AuctionSearchFilter();
        this.page = page;
        setupMenu();
    }

    private void setupMenu() {
        // 出品を取得
        List<AuctionListing> listings = manager.search(filter, SortOrder.CREATED_AT_DESC, page, ITEMS_PER_PAGE);

        // 出品アイテムを表示
        for (int i = 0; i < listings.size() && i < ITEMS_PER_PAGE; i++) {
            AuctionListing listing = listings.get(i);
            setItem(i, createListingItem(listing), player -> {
                new AuctionDetailMenu(manager, listing.getListingId()).open(player);
            });
        }

        // 空きスロットを埋める
        for (int i = listings.size(); i < ITEMS_PER_PAGE; i++) {
            setItem(i, new ItemBuilder().setMaterial(Material.AIR).setName(" "));
        }

        // 下部のナビゲーションバー
        for (int i = 45; i < 54; i++) {
            setItem(i, new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));
        }

        // 前のページ
        if (page > 0) {
            setItem(45, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§a§l前のページ")
                .setClickAction(ClickType.LEFT, player -> {
                    new AuctionListingMenu(manager, filter, page - 1).open(player);
                }));
        }

        // 次のページ
        if (listings.size() == ITEMS_PER_PAGE) {
            setItem(53, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§a§l次のページ")
                .setClickAction(ClickType.LEFT, player -> {
                    new AuctionListingMenu(manager, filter, page + 1).open(player);
                }));
        }

        // 検索ボタン
        setItem(47, new ItemBuilder()
            .setMaterial(Material.COMPASS)
            .setName("§e§l検索条件を変更")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionSearchMenu(manager).open(player);
            }));

        // 戻るボタン
        setItem(49, new ItemBuilder()
            .setMaterial(Material.BARRIER)
            .setName("§c§l戻る")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionMainMenu(manager).open(player);
            }));

        // 更新ボタン
        setItem(51, new ItemBuilder()
            .setMaterial(Material.LIME_DYE)
            .setName("§a§l更新")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionListingMenu(manager, filter, page).open(player);
            }));
    }

    /**
     * 出品アイテムのItemStackを作成
     */
    private ItemStack createListingItem(AuctionListing listing) {
        JsonObject json = gson.fromJson(listing.getArtifactData(), JsonObject.class);
        
        // アイテムマテリアル（スロットに基づいて選択）
        Material material = getMaterialForSlot(json.has("slotId") ? json.get("slotId").getAsInt() : 0);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // 名前
            String seriesName = json.has("seriesName") ? json.get("seriesName").getAsString() : "Unknown";
            int slotId = json.has("slotId") ? json.get("slotId").getAsInt() : 0;
            Slot.artifactSlot slot = Slot.artifactSlot.fromId(slotId);
            String slotName = slot != null ? slot.getSlotName : "Unknown";
            
            meta.displayName(io.github.itokagimaru.artifact.utils.Utils.parseLegacy(
                "§6" + seriesName + " §7- §e" + slotName));
            
            // Lore
            List<String> lore = new ArrayList<>();
            
            // レベル
            int level = json.has("level") ? json.get("level").getAsInt() : 0;
            lore.add("§7Lv.§f" + level);
            lore.add("");
            
            // Main効果
            int mainEffectId = json.has("mainEffectId") ? json.get("mainEffectId").getAsInt() : 0;
            int mainEffectValue = json.has("mainEffectValue") ? json.get("mainEffectValue").getAsInt() : 0;
            MainEffect.artifactMainEffect mainEffect = MainEffect.artifactMainEffect.fromId(mainEffectId);
            String mainEffectName = mainEffect != null ? mainEffect.getText : "Unknown";
            lore.add("§6Main: §f" + mainEffectName + " §a+" + formatPercent(mainEffectValue));
            
            // Sub効果（簡略表示）
            if (json.has("subEffectCount")) {
                int subCount = json.get("subEffectCount").getAsInt();
                lore.add("§eSub効果: §f" + subCount + "個");
            }
            
            lore.add("");
            lore.add("§8━━━━━━━━━━━━━━");
            
            // 価格
            String priceStr = formatPrice(listing.getDisplayPrice());
            if (listing.getType() == AuctionType.BIN) {
                lore.add("§a即時購入: §f$" + priceStr);
            } else {
                if (listing.getCurrentBid() > 0) {
                    lore.add("§e現在入札額: §f$" + priceStr);
                } else {
                    lore.add("§7開始価格: §f$" + priceStr);
                }
            }
            
            // 残り時間
            long remaining = listing.getRemainingTime();
            lore.add("§7残り時間: §f" + formatTime(remaining));
            
            lore.add("");
            lore.add("§eクリックで詳細を表示");
            
            List<net.kyori.adventure.text.Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                componentLore.add(io.github.itokagimaru.artifact.utils.Utils.parseLegacy(line));
            }
            meta.lore(componentLore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private Material getMaterialForSlot(int slotId) {
        return switch (slotId) {
            case 0 -> Material.AMETHYST_SHARD;  // PEAR
            case 1 -> Material.PRISMARINE_SHARD;  // OVAL
            case 2 -> Material.DIAMOND;  // LOZENGE
            case 3 -> Material.EMERALD;  // CLOVER
            case 4 -> Material.LAPIS_LAZULI;  // CUSHION
            case 5 -> Material.QUARTZ;  // CRESCENT
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
