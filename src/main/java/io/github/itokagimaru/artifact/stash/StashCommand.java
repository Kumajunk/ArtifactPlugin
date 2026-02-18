package io.github.itokagimaru.artifact.stash;

import io.github.itokagimaru.artifact.artifact.JsonConverter;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stash command handler.
 * /stash           - Open Stash GUI
 * /stash all       - Withdraw all items
 * /stash count     - Show stash item count
 * /stash add <player> - (Admin) Stash the held artifact into target player's stash
 */
public class StashCommand implements CommandExecutor, TabCompleter {

    private StashManager stashManager;

    public void setStashManager(StashManager stashManager) {
        this.stashManager = stashManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます");
            return true;
        }

        if (stashManager == null) {
            player.sendMessage("§cStashシステムが初期化されていません");
            return true;
        }

        if (args.length == 0) {
            // GUI を開く
            new StashGui(stashManager, 0).open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "all" -> {
                // 全て取り出す
                int count = stashManager.withdrawAll(player);
                if (count > 0) {
                    player.sendMessage("§a" + count + "個のアイテムを取り出しました");
                } else {
                    player.sendMessage("§7取り出せるアイテムがありません");
                }
            }
            case "count" -> {
                // アイテム数を表示
                int count = stashManager.getStashCount(player.getUniqueId());
                player.sendMessage("§e[Stash] §f保管アイテム数: §a" + count);
            }
            case "add" -> handleAddCommand(player, args);
            default -> {
                if (player.hasPermission("artifact.admin")) {
                    player.sendMessage("§c使用方法: /stash [all|count|add <player>]");
                } else {
                    player.sendMessage("§c使用方法: /stash [all|count]");
                }
            }
        }

        return true;
    }

    /**
     * Handle the /stash add <player> subcommand.
     * Requires artifact.admin permission.
     * Takes the artifact from the sender's main hand and stashes it
     * into the target player's stash.
     */
    private void handleAddCommand(Player sender, String[] args) {
        // Permission check
        if (!sender.hasPermission("artifact.admin")) {
            sender.sendMessage("§cこのコマンドを実行する権限がありません");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§c使用方法: /stash add <player>");
            return;
        }

        String targetName = args[1];

        // Resolve target player (supports offline players)
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
        if (!targetOffline.hasPlayedBefore() && !targetOffline.isOnline()) {
            sender.sendMessage("§cプレイヤー \"" + targetName + "\" が見つかりません");
            return;
        }

        // Check if the sender is holding an artifact
        ItemStack heldItem = sender.getInventory().getItemInMainHand();
        Optional<BaseArtifact> artifactOpt = ItemToArtifact.convert(heldItem);

        if (artifactOpt.isEmpty()) {
            sender.sendMessage("§cメインハンドにアーティファクトを持ってください");
            return;
        }

        // Serialize and stash
        BaseArtifact artifact = artifactOpt.get();
        String artifactData = JsonConverter.serializeArtifact(artifact);
        boolean success = stashManager.stashItem(
                targetOffline.getUniqueId(), artifactData, "admin:" + sender.getName());

        if (success) {
            // Remove item from sender's hand
            sender.getInventory().setItemInMainHand(null);
            sender.sendMessage("§a[Stash] §f" + targetName + " のStashにアーティファクトを追加しました");

            // Notify target if online
            Player targetOnline = targetOffline.getPlayer();
            if (targetOnline != null && targetOnline.isOnline()) {
                targetOnline.sendMessage("§e[Stash] §f管理者からアイテムがStashに追加されました。/stash で取り出せます");
            }
        } else {
            sender.sendMessage("§cStashへの保存に失敗しました");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(List.of("all", "count"));
            if (sender.hasPermission("artifact.admin")) {
                completions.add("add");
            }
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(c -> c.startsWith(input))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("add") && sender.hasPermission("artifact.admin")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
