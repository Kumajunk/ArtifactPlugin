package io.github.itokagimaru.artifact.auction.gui;

import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.subEffect.SubEffect;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.model.AuctionType;
import io.github.itokagimaru.artifact.auction.search.AuctionSearchFilter;
import io.github.itokagimaru.artifact.auction.search.SortOrder;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * オークション検索条件を設定するGUI
 */
public class AuctionSearchMenu extends BaseGui {

    private final AuctionManager manager;
    private final AuctionSearchFilter filter;
    private SortOrder sortOrder;

    /**
     * 新規作成用コンストラクタ
     */
    public AuctionSearchMenu(AuctionManager manager) {
        this(manager, new AuctionSearchFilter(), SortOrder.CREATED_AT_DESC);
    }

    /**
     * 状態引き継ぎ用コンストラクタ
     */
    public AuctionSearchMenu(AuctionManager manager, AuctionSearchFilter filter, SortOrder sortOrder) {
        super(54, "§6検索条件");
        this.manager = manager;
        this.filter = filter;
        this.sortOrder = sortOrder;
        setupMenu();
    }

    private void setupMenu() {
        // 背景
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        // シリーズ選択
        setItem(10, new ItemBuilder()
            .setMaterial(Material.DIAMOND)
            .setName("§b§lシリーズ")
            .addLore("§7シリーズで絞り込み")
            .addLore("")
            .addLore(getSeriesDescription())
            .addLore("")
            .addLore("§e左クリック: 次のシリーズ")
            .addLore("§e右クリック: クリア")
            .setClickAction(ClickType.LEFT, player -> {
                cycleSeriesFilter();
                refresh(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                filter.setSeries(new java.util.ArrayList<>());
                refresh(player);
            }));

        // スロット選択
        setItem(11, new ItemBuilder()
            .setMaterial(Material.EMERALD)
            .setName("§a§lスロット")
            .addLore("§7スロットで絞り込み")
            .addLore("")
            .addLore(getSlotDescription())
            .addLore("")
            .addLore("§e左クリック: 次のスロット")
            .addLore("§e右クリック: クリア")
            .setClickAction(ClickType.LEFT, player -> {
                cycleSlotFilter();
                refresh(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                filter.setSlot(null);
                refresh(player);
            }));

        // レベル範囲
        setItem(12, new ItemBuilder()
            .setMaterial(Material.EXPERIENCE_BOTTLE)
            .setName("§e§l強化レベル")
            .addLore("§7レベル範囲で絞り込み")
            .addLore("")
            .addLore(getLevelDescription())
            .addLore("")
            .addLore("§e左クリック: 最小値+1")
            .addLore("§e右クリック: 最大値+1")
            .addLore("§eシフト+クリック: リセット")
            .setClickAction(ClickType.LEFT, player -> {
                int min = filter.getLevelMin() != null ? filter.getLevelMin() + 1 : 0;
                if (min > 30) min = 0;
                if (filter.getLevelMax() != null && filter.getLevelMax() < min) {
                    min = filter.getLevelMax();
                };
                filter.setLevelRange(min, filter.getLevelMax());
                refresh(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                int max = filter.getLevelMax() != null ? filter.getLevelMax() + 1 : 5;
                if (max > 30) max = 5;
                if (filter.getLevelMin() != null && filter.getLevelMin() > max) {
                    max = filter.getLevelMin();
                }
                filter.setLevelRange(filter.getLevelMin(), max);
                refresh(player);
            })
            .setClickAction(ClickType.SHIFT_LEFT, player -> {
                filter.setLevelRange(null, null);
                refresh(player);
            }));

        // Main効果選択
        setItem(19, new ItemBuilder()
            .setMaterial(Material.GOLDEN_APPLE)
            .setName("§6§lMain効果")
            .addLore("§7Main効果で絞り込み")
            .addLore("")
            .addLore(getMainEffectDescription())
            .addLore("")
            .addLore("§e左クリック: 次の効果")
            .addLore("§e右クリック: クリア")
            .setClickAction(ClickType.LEFT, player -> {
                cycleMainEffectFilter();
                refresh(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                filter.setMainEffect(null);
                refresh(player);
            }));

        // Sub効果（含む）
        setItem(20, new ItemBuilder()
            .setMaterial(Material.APPLE)
            .setName("§c§lSub効果（含む）")
            .addLore("§7含まれるべきSub効果")
            .addLore("")
            .addLore(getRequiredSubEffectsDescription())
            .addLore("")
            .addLore("§e左クリック: 効果を追加する")
            .addLore("§e右クリック: クリア")
            .setClickAction(ClickType.LEFT, player -> {
                new RequiredSubEffectMenu(manager, filter, sortOrder).open(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                filter.getRequiredSubEffects().clear();
                refresh(player);
            }));

        // Sub効果（除外）
        setItem(21, new ItemBuilder()
            .setMaterial(Material.POISONOUS_POTATO)
            .setName("§8§lSub効果（除外）")
            .addLore("§7除外するSub効果")
            .addLore("")
            .addLore(getExcludedSubEffectsDescription())
            .addLore("")
            .addLore("§e左クリック: 効果を追加")
            .addLore("§e右クリック: クリア")
            .setClickAction(ClickType.LEFT, player -> {
                new ExcludedSubEffectMenu(manager, filter, sortOrder).open(player);
            })
            .setClickAction(ClickType.RIGHT, player -> {
                filter.getExcludedSubEffects().clear();
                refresh(player);
            }));

        // オークション種別
        setItem(28, new ItemBuilder()
            .setMaterial(Material.CLOCK)
            .setName("§d§lオークション種別")
            .addLore("§7BIN / オークション")
            .addLore("")
            .addLore(getAuctionTypeDescription())
            .addLore("")
            .addLore("§eクリックで切り替え")
            .setClickAction(ClickType.LEFT, player -> {
                cycleAuctionType();
                refresh(player);
            }));

        // ソート順
        setItem(29, new ItemBuilder()
            .setMaterial(Material.HOPPER)
            .setName("§5§lソート順")
            .addLore("§7並び替え順序")
            .addLore("")
            .addLore("§f現在: §e" + sortOrder.getDisplayName())
            .addLore("")
            .addLore("§eクリックで切り替え")
            .setClickAction(ClickType.LEFT, player -> {
                cycleSortOrder();
                refresh(player);
            }));

        // リセットボタン
        setItem(39, new ItemBuilder()
            .setMaterial(Material.TNT)
            .setName("§c§lリセット")
            .addLore("§7すべての条件をクリア")
            .setClickAction(ClickType.LEFT, player -> {
                filter.reset();
                sortOrder = SortOrder.CREATED_AT_DESC;
                refresh(player);
            }));

        // 検索実行ボタン
        setItem(41, new ItemBuilder()
            .setMaterial(Material.LIME_WOOL)
            .setName("§a§l検索実行")
            .addLore("§7この条件で検索します")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionListingMenu(manager, filter, 0).open(player);
            }));

        // 戻るボタン
        setItem(49, new ItemBuilder()
            .setMaterial(Material.ARROW)
            .setName("§7§l戻る")
            .setClickAction(ClickType.LEFT, player -> {
                new AuctionMainMenu(manager).open(player);
            }));
    }

    /**
     * GUIを更新する（状態を保持したまま再描画）
     */
    private void refresh(Player player) {
        // 状態を引き継いで新しいGUIを作成
        new AuctionSearchMenu(manager, filter, sortOrder).open(player);
    }

    // ========== ヘルパーメソッド ==========

    private String getSeriesDescription() {
        if (filter.getSeries().isEmpty()) {
            return "§7指定なし";
        }
        StringBuilder sb = new StringBuilder("§f");
        for (int i = 0; i < filter.getSeries().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(filter.getSeries().get(i).name());
        }
        return sb.toString();
    }

    private void cycleSeriesFilter() {
        Series.artifactSeres[] values = Series.artifactSeres.values();
        if (filter.getSeries().isEmpty()) {
            filter.addSeries(values[0]);
        } else {
            Series.artifactSeres current = filter.getSeries().get(filter.getSeries().size() - 1);
            int nextIndex = (current.ordinal() + 1) % values.length;
            filter.getSeries().clear();
            filter.addSeries(values[nextIndex]);
        }
    }

    private String getSlotDescription() {
        if (filter.getSlot() == null) {
            return "§7指定なし";
        }
        return "§f" + filter.getSlot().getSlotName;
    }

    private void cycleSlotFilter() {
        Slot.artifactSlot[] values = Slot.artifactSlot.values();
        if (filter.getSlot() == null) {
            filter.setSlot(values[0]);
        } else {
            int nextIndex = (filter.getSlot().ordinal() + 1) % values.length;
            filter.setSlot(values[nextIndex]);
        }
    }

    private String getLevelDescription() {
        Integer min = filter.getLevelMin();
        Integer max = filter.getLevelMax();
        if (min == null && max == null) {
            return "§7指定なし";
        }
        return "§fLv." + (min != null ? min : "0") + " ～ Lv." + (max != null ? max : "30");
    }

    private String getMainEffectDescription() {
        if (filter.getMainEffect() == null) {
            return "§7指定なし";
        }
        return "§f" + filter.getMainEffect().getText;
    }

    private void cycleMainEffectFilter() {
        MainEffect.artifactMainEffect[] values = MainEffect.artifactMainEffect.values();
        if (filter.getMainEffect() == null) {
            filter.setMainEffect(values[0]);
        } else {
            int nextIndex = (filter.getMainEffect().ordinal() + 1) % values.length;
            filter.setMainEffect(values[nextIndex]);
        }
    }

    private String getRequiredSubEffectsDescription() {
        if (filter.getRequiredSubEffects().isEmpty()) {
            return "§7指定なし";
        }
        StringBuilder sb = new StringBuilder("§f");
        for (int i = 0; i < filter.getRequiredSubEffects().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(filter.getRequiredSubEffects().get(i).getText);
        }
        return sb.toString();
    }

    private void addRequiredSubEffect() {
        SubEffect.artifactSubEffect[] values = SubEffect.artifactSubEffect.values();
        for (SubEffect.artifactSubEffect effect : values) {
            if (!filter.getRequiredSubEffects().contains(effect)) {
                filter.addRequiredSubEffect(effect);
                return;
            }
        }
    }

    private String getExcludedSubEffectsDescription() {
        if (filter.getExcludedSubEffects().isEmpty()) {
            return "§7指定なし";
        }
        StringBuilder sb = new StringBuilder("§f");
        for (int i = 0; i < filter.getExcludedSubEffects().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(filter.getExcludedSubEffects().get(i).getText);
        }
        return sb.toString();
    }

    private void addExcludedSubEffect() {
        SubEffect.artifactSubEffect[] values = SubEffect.artifactSubEffect.values();
        for (SubEffect.artifactSubEffect effect : values) {
            if (!filter.getExcludedSubEffects().contains(effect)) {
                filter.addExcludedSubEffect(effect);
                return;
            }
        }
    }

    private String getAuctionTypeDescription() {
        if (filter.getAuctionType() == null) {
            return "§7すべて";
        }
        return "§f" + filter.getAuctionType().getDisplayName();
    }

    private void cycleAuctionType() {
        if (filter.getAuctionType() == null) {
            filter.setAuctionType(AuctionType.BIN);
        } else if (filter.getAuctionType() == AuctionType.BIN) {
            filter.setAuctionType(AuctionType.AUCTION);
        } else {
            filter.setAuctionType(null);
        }
    }

    private void cycleSortOrder() {
        SortOrder[] values = SortOrder.values();
        int nextIndex = (sortOrder.ordinal() + 1) % values.length;
        sortOrder = values[nextIndex];
    }
}
