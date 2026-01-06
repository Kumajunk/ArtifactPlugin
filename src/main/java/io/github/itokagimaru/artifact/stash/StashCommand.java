package io.github.itokagimaru.artifact.stash;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Stashコマンド
 * 
 * /stash - Stash GUIを開く
 * /stash all - 全てのアイテムを取り出す
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
            default -> {
                player.sendMessage("§c使用方法: /stash [all|count]");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("all", "count");
        }
        return new ArrayList<>();
    }
}
