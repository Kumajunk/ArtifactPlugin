package io.github.itokagimaru.artifact.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BaseGui {

    private final Inventory inventory;
    private final Map<Integer, Map<ClickType, GuiAction>> actions = new HashMap<>();
    private final Set<Integer> returnSlots = new HashSet<>();
    private boolean returnItemsOnClose = false;

    public BaseGui(int size, String title) {
        this.inventory = Bukkit.createInventory(null, size, Utils.parseLegacy(title));
    }

    public void setReturnItemsOnClose(boolean returnItemsOnClose) {
        this.returnItemsOnClose = returnItemsOnClose;
    }

    public interface GuiAction {
        void run(Player player);
    }

    public interface PlayerInventoryClickHandler {
        void onClick(Player player, int slot, ItemStack item, ClickType clickType);
    }

    private PlayerInventoryClickHandler playerInventoryClickHandler;

    public void setPlayerInventoryClickHandler(PlayerInventoryClickHandler handler) {
        this.playerInventoryClickHandler = handler;
    }

    public PlayerInventoryClickHandler getPlayerInventoryClickHandler() {
        return playerInventoryClickHandler;
    }

    public interface GuiCloseAction {
        void run(Player player);
    }

    private final List<GuiCloseAction> closeActions = new ArrayList<>();

    public void addCloseAction(GuiCloseAction action) {
        closeActions.add(action);
    }

    public List<GuiCloseAction> getCloseActions() {
        return closeActions;
    }

    private static JavaPlugin instance;
    private static final Map<UUID, BaseGui> openGuis = new HashMap<>();

    public static void setup(JavaPlugin plugin) {
        instance = plugin;
    }

    public static void openGui(Player player, BaseGui gui) {
        openGuis.put(player.getUniqueId(), gui);
        if (player.getOpenInventory().getTopInventory() != gui.inventory) {
            player.openInventory(gui.inventory);
        }
    }

    public static BaseGui getGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    public static void removeGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    public void open(Player player) {
        openGui(player, this);
    }

    public void setItem(int slot, ItemBuilder builder) {
        inventory.setItem(slot, builder.build());
        actions.put(slot, new HashMap<>(builder.clickActions));
    }

    public void setItem(int slot, ItemStack item, GuiAction action) {
        inventory.setItem(slot, item);
        if (action != null) {
            Map<ClickType, GuiAction> map = new HashMap<>();
            map.put(ClickType.LEFT, action);
            actions.put(slot, map);
        }
    }

    public void setReturnItem(int slot, ItemBuilder builder) {
        returnItemsOnClose = true;
        setItem(slot, builder);
        returnSlots.add(slot);
    }

    public void setReturnItem(int slot, ItemStack item, GuiAction action) {
        returnItemsOnClose = true;
        setItem(slot, item, action);
        returnSlots.add(slot);
    }

    public void setTemporaryButton(int slot, ItemStack item, long displayTime) {
        ItemStack original = inventory.getItem(slot);
        if (original != null) {
            original = original.clone();
        }

        inventory.setItem(slot, item);

        ItemStack finalOriginal = original;
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            inventory.setItem(slot, finalOriginal);
        }, displayTime);
    }

    public void fill(ItemBuilder builder) {
        for (int i = 0; i < inventory.getSize(); i++) {
            setItem(i, builder);
        }
    }

    public static class GuiListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            BaseGui gui = getGui(player);
            if (gui == null) return;

            if (event.getInventory() != gui.inventory) return;

            boolean isTopInventory = event.getRawSlot() < gui.inventory.getSize();

            if (isTopInventory) {
                event.setCancelled(true);

                Map<ClickType, GuiAction> map = gui.actions.get(event.getSlot());
                if (map == null) return;

                GuiAction action = map.get(event.getClick());
                if (action != null) {
                    action.run(player);
                }
            } else {
                PlayerInventoryClickHandler handler = gui.getPlayerInventoryClickHandler();
                if (handler != null) {
                    event.setCancelled(true);
                    int playerSlot = event.getSlot();
                    ItemStack clickedItem = event.getCurrentItem();
                    handler.onClick(player, playerSlot, clickedItem, event.getClick());
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (!(event.getPlayer() instanceof Player player)) return;

            BaseGui gui = getGui(player);
            if (gui == null) return;

            if (event.getInventory() != gui.inventory) return;

            if (gui.returnItemsOnClose && event.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
                for (int slot : gui.returnSlots) {
                    ItemStack item = gui.inventory.getItem(slot);
                    if (item == null || item.getType() == Material.AIR) continue;

                    Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                    if (!remaining.isEmpty()) {
                        remaining.values().forEach(i ->
                                player.getWorld().dropItemNaturally(player.getLocation(), i)
                        );
                    }
                }
                gui.inventory.clear();
            }

            if (event.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
                for (GuiCloseAction action : gui.getCloseActions()) {
                    action.run(player);
                }
            }

            removeGui(player);
        }
    }

    public static class ItemBuilder {

        private Material material = Material.AIR;
        private String name;
        private final List<String> lore = new ArrayList<>();
        private final Map<ClickType, GuiAction> clickActions = new HashMap<>();

        public ItemBuilder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public ItemBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder addLore(String line) {
            lore.add(line);
            return this;
        }

        public ItemBuilder setLore(List<String> lines) {
            lore.clear();
            lore.addAll(lines);
            return this;
        }

        public ItemBuilder setClickAction(ClickType type, GuiAction action) {
            clickActions.put(type, action);
            return this;
        }

        public ItemStack build() {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (name != null) {
                    meta.displayName(Utils.parseLegacy(name));
                }
                if (!lore.isEmpty()) {
                    List<Component> parsedLore = new ArrayList<>();
                    for (String line : lore) {
                        parsedLore.add(Utils.parseLegacy(line));
                    }
                    meta.lore(parsedLore);
                }
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}