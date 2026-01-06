package io.github.itokagimaru.artifact.stash;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.itokagimaru.artifact.artifact.artifacts.data.mainEffect.MainEffect;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * InventoryStashのGUI
 * 
 * 保管されているアイテムを表示し、取り出し操作を提供する。
 */
public class StashGui extends BaseGui {

    private final StashManager stashManager;
    private final int page;
    private static final int ITEMS_PER_PAGE = 45;
    private final Gson gson = new Gson();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public StashGui(StashManager stashManager, int page) {
        super(54, "§d§lInventory Stash - ページ " + (page + 1));
        this.stashManager = stashManager;
        this.page = page;
    }

    @Override
    public void open(Player player) {
        setupMenu(player);
        super.open(player);
    }

    private void setupMenu(Player player) {
        List<StashItem> items = stashManager.getPlayerStash(player.getUniqueId());

        // ページングを適用
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        // アイテムを表示
        for (int i = startIndex; i < endIndex; i++) {
            StashItem stashItem = items.get(i);
            int slot = i - startIndex;
            setItem(slot, createStashItemDisplay(stashItem), p -> {
                handleWithdraw(p, stashItem);
            });
        }

        // 空きスロットを埋める
        for (int i = endIndex - startIndex; i < ITEMS_PER_PAGE; i++) {
            setItem(i, new ItemBuilder().setMaterial(Material.AIR).setName(" "));
        }

        // 下部ナビゲーション
        for (int i = 45; i < 54; i++) {
            setItem(i, new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));
        }

        // 前のページ
        if (page > 0) {
            setItem(45, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§a前のページ")
                .setClickAction(ClickType.LEFT, p -> {
                    new StashGui(stashManager, page - 1).open(p);
                }));
        }

        // 情報
        setItem(49, new ItemBuilder()
            .setMaterial(Material.CHEST)
            .setName("§e§lStash情報")
            .addLore("§f保管アイテム数: §a" + items.size())
            .addLore("")
            .addLore("§7アイテムをクリックで取り出し"));

        // 全て取り出す
        if (!items.isEmpty()) {
            setItem(51, new ItemBuilder()
                .setMaterial(Material.HOPPER)
                .setName("§a§l全て取り出す")
                .addLore("§7インベントリに空きがある分だけ")
                .addLore("§7取り出します")
                .setClickAction(ClickType.LEFT, p -> {
                    handleWithdrawAll(p);
                }));
        }

        // 次のページ
        if (endIndex < items.size()) {
            setItem(53, new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("§a次のページ")
                .setClickAction(ClickType.LEFT, p -> {
                    new StashGui(stashManager, page + 1).open(p);
                }));
        }
    }

    private ItemStack createStashItemDisplay(StashItem stashItem) {
        JsonObject json = gson.fromJson(stashItem.getItemData(), JsonObject.class);

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

            // 入手元
            lore.add(Utils.parseLegacy("§7入手元: §f" + stashItem.getSourceDisplayName()));

            // 保管日時
            lore.add(Utils.parseLegacy("§7保管日時: §f" + dateFormat.format(new Date(stashItem.getCreatedAt()))));

            lore.add(Utils.parseLegacy(""));
            lore.add(Utils.parseLegacy("§eクリックで取り出し"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void handleWithdraw(Player player, StashItem stashItem) {
        boolean success = stashManager.withdrawItem(player, stashItem.getId());
        // GUIを更新
        new StashGui(stashManager, page).open(player);
    }

    private void handleWithdrawAll(Player player) {
        int count = stashManager.withdrawAll(player);
        if (count > 0) {
            player.sendMessage("§a" + count + "個のアイテムを取り出しました");
        }
        // GUIを更新
        new StashGui(stashManager, page).open(player);
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
}
