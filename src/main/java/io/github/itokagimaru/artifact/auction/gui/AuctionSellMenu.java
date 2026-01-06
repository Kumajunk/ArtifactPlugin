package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.series.Base.BaseArtifact;
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

import java.util.Optional;

/**
 * 出品画面GUI
 * 
 * プレイヤーがアーティファクトを出品するためのインターフェース。
 * インベントリ内のアイテムをクリックして選択し、出品確定時にのみ消費される。
 */
public class AuctionSellMenu extends BaseGui {

    private final AuctionManager manager;
    private AuctionType selectedType;
    private int selectedDuration;
    
    // 選択されたアイテムの情報
    private ItemStack selectedItem;          // 選択されたアイテムのコピー
    private int selectedSlotIndex = -1;      // 元アイテムのインベントリ内スロット番号

    // アイテム表示用スロット番号
    private static final int ITEM_DISPLAY_SLOT = 22;

    /**
     * 新規作成用コンストラクタ
     */
    public AuctionSellMenu(AuctionManager manager) {
        this(manager, AuctionType.BIN, 48, null, -1);
    }

    /**
     * 状態引き継ぎ用コンストラクタ
     */
    public AuctionSellMenu(AuctionManager manager, AuctionType selectedType, int selectedDuration) {
        this(manager, selectedType, selectedDuration, null, -1);
    }

    /**
     * 選択状態を含む完全なコンストラクタ
     */
    public AuctionSellMenu(AuctionManager manager, AuctionType selectedType, int selectedDuration, 
                           ItemStack selectedItem, int selectedSlotIndex) {
        super(54, "§6§lアーティファクトを出品");
        this.manager = manager;
        this.selectedType = selectedType;
        this.selectedDuration = selectedDuration;
        this.selectedItem = selectedItem != null ? selectedItem.clone() : null;
        this.selectedSlotIndex = selectedSlotIndex;
        setupMenu();
        setupPlayerInventoryHandler();
    }

    /**
     * プレイヤーインベントリのクリックハンドラーを設定
     */
    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            // 空のスロットは無視
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            // アーティファクトかどうか判定
            if (!ItemToArtifact.isArtifact(item)) {
                player.sendMessage("§cこのアイテムはアーティファクトではありません");
                return;
            }

            // アイテムを選択
            selectItem(player, item.clone(), slot);
        });
    }

    /**
     * アイテムを選択する
     */
    private void selectItem(Player player, ItemStack item, int slotIndex) {
        this.selectedItem = item;
        this.selectedSlotIndex = slotIndex;
        
        player.sendMessage("§aアイテムを選択しました！");
        
        // GUIを更新して選択されたアイテムを表示
        new AuctionSellMenu(manager, selectedType, selectedDuration, selectedItem, selectedSlotIndex).open(player);
    }

    private void setupMenu() {
        // 背景
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // 説明
        setItem(4, new ItemBuilder()
            .setMaterial(Material.BOOK)
            .setName("§e§l出品方法")
            .addLore("§71. 下のインベントリから")
            .addLore("§7   出品するアーティファクトをクリック")
            .addLore("§72. 出品タイプを選択")
            .addLore("§73. 期間を選択")
            .addLore("§74. 価格を入力")
            .addLore("")
            .addLore("§c※アイテムは出品確定時にのみ消費されます"));

        // アイテム配置スロット
        if (selectedItem != null) {
            // 選択されたアイテムを表示
            ItemStack displayItem = selectedItem.clone();
            setItem(ITEM_DISPLAY_SLOT, displayItem, player -> {
                // クリックで選択解除
                player.sendMessage("§7選択を解除しました");
                new AuctionSellMenu(manager, selectedType, selectedDuration).open(player);
            });
        } else {
            // 選択されていない場合のプレースホルダー
            setItem(ITEM_DISPLAY_SLOT, new ItemBuilder()
                .setMaterial(Material.YELLOW_STAINED_GLASS_PANE)
                .setName("§e§l出品アイテム")
                .addLore("§7下のインベントリから")
                .addLore("§7出品するアーティファクトを")
                .addLore("§7クリックしてください")
                .addLore("")
                .addLore("§eアイテムをクリックで選択"));
        }

        // 出品タイプ選択
        setItem(29, new ItemBuilder()
            .setMaterial(selectedType == AuctionType.BIN ? Material.GOLD_INGOT : Material.CLOCK)
            .setName("§d§l出品タイプ")
            .addLore("§f現在: §e" + selectedType.getDisplayName())
            .addLore("")
            .addLore("§eBIN: §7即時購入方式")
            .addLore("§eオークション: §7入札方式")
            .addLore("")
            .addLore("§eクリックで切り替え")
            .setClickAction(ClickType.LEFT, player -> {
                selectedType = selectedType == AuctionType.BIN ? AuctionType.AUCTION : AuctionType.BIN;
                refresh(player);
            }));

        // 期間選択
        setItem(31, new ItemBuilder()
            .setMaterial(Material.CLOCK)
            .setName("§b§l出品期間")
            .addLore("§f現在: §e" + selectedDuration + "時間")
            .addLore("")
            .addLore("§e左クリック: +1時間")
            .addLore("§e右クリック: -1時間")
            .addLore("§eSHIFT+左クリック: +12時間")
            .addLore("§eSHIFT+右クリック: -12時間")
            .addLore("§7（最小: 12時間、最大: 168時間）")
            .setClickAction(ClickType.LEFT, player -> {
                selectedDuration = Math.min(168, selectedDuration + 1);
                refresh(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                selectedDuration = Math.max(12, selectedDuration - 1);
                refresh(player);
            })
            .setClickAction(ClickType.SHIFT_LEFT, player -> {
                selectedDuration = Math.min(168, selectedDuration + 12);
                refresh(player);
            })
            .setClickAction(ClickType.SHIFT_RIGHT, player -> {
                selectedDuration = Math.max(12, selectedDuration - 12);
                refresh(player);
            }));

        // 出品手数料
        setItem(33, new ItemBuilder()
            .setMaterial(Material.SUNFLOWER)
            .setName("§6§l手数料")
            .addLore("§7出品時手数料: §e5%")
            .addLore("§7成立時手数料: §e10%")
            .addLore("")
            .addLore("§8※価格入力後に確認できます"));

        // 価格入力ボタン
        setItem(40, new ItemBuilder()
            .setMaterial(selectedItem != null ? Material.LIME_WOOL : Material.GRAY_WOOL)
            .setName(selectedItem != null ? "§a§l価格を入力して出品" : "§7§l価格を入力して出品")
            .addLore(selectedItem != null ? "§7チャットで価格を入力します" : "§c先にアイテムを選択してください")
            .addLore("")
            .addLore(selectedItem != null ? "§eクリックで開始" : "§7クリック不可")
            .setClickAction(ClickType.LEFT, this::handlePriceInput));

        // 戻るボタン
        setItem(49, new ItemBuilder()
            .setMaterial(Material.ARROW)
            .setName("§7§l戻る")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionMainMenu(manager).open(player);
            }));
    }

    /**
     * 価格入力を処理する
     */
    private void handlePriceInput(Player player) {
        // アイテムが選択されているか確認
        if (selectedItem == null || selectedSlotIndex < 0) {
            player.sendMessage("§c出品するアーティファクトを選択してください");
            return;
        }

        // 元のスロットにアイテムがまだあるか確認
        ItemStack currentItem = player.getInventory().getItem(selectedSlotIndex);
        if (currentItem == null || !currentItem.isSimilar(selectedItem)) {
            player.sendMessage("§c選択されたアイテムが見つかりません。再度選択してください");
            selectedItem = null;
            selectedSlotIndex = -1;
            refresh(player);
            return;
        }

        // アーティファクトかどうか判定
        if (!ItemToArtifact.isArtifact(selectedItem)) {
            player.sendMessage("§c選択されたアイテムはアーティファクトではありません");
            return;
        }

        // アーティファクトを復元
        Optional<BaseArtifact> optArtifact = ItemToArtifact.convert(selectedItem);
        if (optArtifact.isEmpty()) {
            player.sendMessage("§cアーティファクトの読み込みに失敗しました");
            return;
        }

        BaseArtifact artifact = optArtifact.get();
        
        player.closeInventory();
        
        Utils.promptTextInput(
            player,
            "出品価格を入力してください（数字のみ）",
            15,
            input -> {
                try {
                    long price = Long.parseLong(input.replaceAll("[^0-9]", ""));
                    
                    if (price <= 0) {
                        player.sendMessage("§c価格は1以上である必要があります");
                        new AuctionSellMenu(manager, selectedType, selectedDuration, selectedItem, selectedSlotIndex).open(player);
                        return;
                    }
                    
                    // 手数料計算
                    long listingFee = Math.round(price * 0.05);
                    
                    // 確認メッセージを表示
                    player.sendMessage("§6§l=== 出品確認 ===");
                    player.sendMessage("§fアイテム: §e" + artifact.getSeriesName() + " - " + artifact.getSlot().getSlotName);
                    player.sendMessage("§fレベル: §a" + artifact.getLv());
                    player.sendMessage("§f出品タイプ: §e" + selectedType.getDisplayName());
                    player.sendMessage("§f出品価格: §a$" + String.format("%,d", price));
                    player.sendMessage("§f出品期間: §b" + selectedDuration + "時間");
                    player.sendMessage("§f出品手数料: §c$" + String.format("%,d", listingFee));
                    player.sendMessage("");
                    player.sendMessage("§e「confirm」と入力して確定、その他でキャンセル");
                    
                    // 確認入力を待つ
                    promptConfirmation(player, artifact, price);
                    
                } catch (NumberFormatException e) {
                    player.sendMessage("§c無効な価格です。数字を入力してください");
                    new AuctionSellMenu(manager, selectedType, selectedDuration, selectedItem, selectedSlotIndex).open(player);
                }
            }
        );
    }

    /**
     * 出品確認を処理する
     */
    private void promptConfirmation(Player player, BaseArtifact artifact, long price) {
        Utils.promptTextInput(
            player,
            "「confirm」で確定、その他でキャンセル",
            10,
            input -> {
                if (input.equalsIgnoreCase("confirm")) {
                    // 元のスロットにアイテムがまだあるか最終確認
                    ItemStack currentItem = player.getInventory().getItem(selectedSlotIndex);
                    if (currentItem == null || !currentItem.isSimilar(selectedItem)) {
                        player.sendMessage("§c選択されたアイテムが見つかりません。再度やり直してください");
                        new AuctionMainMenu(manager).open(player);
                        return;
                    }

                    // 出品実行
                    Result<AuctionListing> result = manager.createListing(
                        player, 
                        artifact, 
                        selectedType, 
                        price, 
                        selectedDuration
                    );

                    if (result.isSuccess()) {
                        // 出品成功時、元のスロットからアイテムを削除
                        player.getInventory().setItem(selectedSlotIndex, null);
                        player.sendMessage("§a出品が完了しました!");
                        player.sendMessage("§7出品ID: §f" + result.getData().getListingId());
                    } else {
                        player.sendMessage("§c出品に失敗しました: " + result.getErrorMessage());
                    }
                } else {
                    player.sendMessage("§c出品をキャンセルしました");
                }
                new AuctionMainMenu(manager).open(player);
            }
        );
    }

    /**
     * GUIを更新する（状態を保持したまま再描画）
     */
    private void refresh(Player player) {
        new AuctionSellMenu(manager, selectedType, selectedDuration, selectedItem, selectedSlotIndex).open(player);
    }
}
