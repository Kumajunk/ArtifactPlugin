package io.github.itokagimaru.artifact.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class VaultAPI {

    private final JavaPlugin plugin;
    private static Economy economy;

    public VaultAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public void setupEconomy() {
        plugin.getLogger().info("Setting up VaultAPI...");

        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin is not installed");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Can't get vault service!");
            return;
        }

        economy = rsp.getProvider();
        plugin.getLogger().info("VaultAPI setup complete");
    }

    public double getBalance(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return economy.getBalance(player);
    }

    public boolean deposit(UUID uuid, double money) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        EconomyResponse resp = economy.depositPlayer(player, money);

        if (resp != null && resp.transactionSuccess()) {
            if (player.isOnline()) {
                String formatted = NumberFormat.getNumberInstance(Locale.US).format(money);
                player.getPlayer().sendMessage(format("<yellow>[Vault] $<green>" + formatted + " <yellow>受取りました"));
            }
            return true;
        }
        return false;
    }

    public boolean withdraw(UUID uuid, double money) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        EconomyResponse resp = economy.withdrawPlayer(player, money);

        if (resp != null && resp.transactionSuccess()) {
            if (player.isOnline()) {
                String formatted = NumberFormat.getNumberInstance(Locale.US).format(money);
                player.getPlayer().sendMessage(format("<yellow>[Vault] $<red>" + formatted + " <yellow>支払いました"));
            }
            return true;
        }
        return false;
    }

    private Component format(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
