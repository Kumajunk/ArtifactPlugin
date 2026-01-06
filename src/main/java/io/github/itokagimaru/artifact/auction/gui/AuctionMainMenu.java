package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * オークションのメインメニューGUI
 * 
 * 各機能へのナビゲーションを提供する。
 */
public class AuctionMainMenu extends BaseGui {

    private final AuctionManager manager;

    /**
     * コンストラクタ
     * 
     * @param manager オークションマネージャー
     */
    public AuctionMainMenu(AuctionManager manager) {
        super(54, "§6§lアーティファクト・オークション");
        this.manager = manager;
        setupMenu();
    }

    /**
     * メニューをセットアップする
     */
    private void setupMenu() {
        // 背景を埋める
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // 検索ボタン
        setItem(20, new ItemBuilder()
            .setMaterial(Material.COMPASS)
            .setName("§a§l検索")
            .addLore("§7条件を指定して出品を検索します")
            .addLore("")
            .addLore("§eクリックで開く")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionSearchMenu(manager).open(player);
            }));

        // 一覧表示ボタン
        setItem(22, new ItemBuilder()
            .setMaterial(Material.BOOK)
            .setName("§b§l出品一覧")
            .addLore("§7すべての出品を表示します")
            .addLore("")
            .addLore("§eクリックで開く")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionListingMenu(manager, null, 0).open(player);
            }));

        // 出品ボタン
        setItem(24, new ItemBuilder()
            .setMaterial(Material.GOLD_INGOT)
            .setName("§6§l出品する")
            .addLore("§7アーティファクトを出品します")
            .addLore("")
            .addLore("§eクリックで開く")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionSellMenu(manager).open(player);
            }));

        // 自分の出品ボタン
        setItem(29, new ItemBuilder()
            .setMaterial(Material.ENDER_CHEST)
            .setName("§d§l自分の出品")
            .addLore("§7あなたが出品中のアイテム")
            .addLore("")
            .addLore("§eクリックで開く")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionMyListingsMenu(manager).open(player);
            }));

        // 購入履歴ボタン（将来実装）
        setItem(33, new ItemBuilder()
            .setMaterial(Material.PAPER)
            .setName("§e§l取引履歴")
            .addLore("§7過去の取引を確認します")
            .addLore("")
            .addLore("§c近日実装予定")
            .setClickAction(ClickType.LEFT, player -> {
                player.sendMessage("§c取引履歴機能は近日実装予定です");
            }));

        // 閉じるボタン
        setItem(49, new ItemBuilder()
            .setMaterial(Material.BARRIER)
            .setName("§c§l閉じる")
            .setClickAction(ClickType.LEFT, Player::closeInventory));
    }
}
