package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.model.AuctionListing;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
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
        // Attempt to restore the artifact and use the canonical item representation
        BaseArtifact artifact = JsonConverter.deserializeArtifact(listing.getArtifactData());
        if (artifact != null) {
            ItemStack item = ArtifactToItem.convert(artifact);
            appendAuctionLore(item, listing);
            return item;
        }

        // Fallback: if deserialization fails, show a placeholder
        ItemStack fallback = new ItemStack(Material.BARRIER);
        fallback.editMeta(meta -> {
            meta.displayName(Component.text("データ復元失敗").color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
        });
        return fallback;
    }

    /**
     * Append auction-specific information to an existing artifact ItemStack's lore.
     */
    private void appendAuctionLore(ItemStack item, AuctionListing listing) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        // Separator
        lore.add(Component.text("━━━━━ AUCTION ━━━━━").color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        // Price
        String priceStr = formatPrice(listing.getDisplayPrice());
        if (listing.getType() == AuctionType.BIN) {
            lore.add(Utils.parseLegacy("§a即時購入: §f$" + priceStr));
        } else {
            if (listing.getCurrentBid() > 0) {
                lore.add(Utils.parseLegacy("§e現在入札額: §f$" + priceStr));
            } else {
                lore.add(Utils.parseLegacy("§7開始価格: §f$" + priceStr));
            }
        }

        // Remaining time
        long remaining = listing.getRemainingTime();
        lore.add(Utils.parseLegacy("§7残り時間: §f" + formatTime(remaining)));

        lore.add(Component.empty());
        lore.add(Utils.parseLegacy("§eクリックで詳細を表示"));

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
}