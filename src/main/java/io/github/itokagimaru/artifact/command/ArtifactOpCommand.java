package io.github.itokagimaru.artifact.command;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.gui.LootTableEditMenu;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理者用コマンド (/artifactop)
 * artifact.admin権限が必要
 */
public class ArtifactOpCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("artifact.admin")) {
            sender.sendMessage("§cこのコマンドを実行する権限がありません");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> {
                showHelp(sender);
                return true;
            }
            case "decomptable" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます");
                    return true;
                }
                new LootTableEditMenu().open(player);
                return true;
            }
            case "getaugment" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます");
                    return true;
                }
                
                int count = 1;
                if (args.length > 1) {
                    try {
                        count = Integer.parseInt(args[1]);
                        if (count <= 0) {
                            player.sendMessage("§c数は1以上を指定してください");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c有効な数字を入力してください: " + args[1]);
                        return true;
                    }
                }

                for (int i = 0; i < count; i++) {
                    ItemStack augment = SpecialItems.getAugment();
                    player.getInventory().addItem(augment);
                }
                player.sendMessage("§aオーグメントを" + count + "個取得しました");
                return true;
            }
            case "reload" -> {
                ArtifactMain.getGeneralConfig().reload();
                ArtifactMain.getDecomposeConfig().reload();
                sender.sendMessage("§a設定をリロードしました");
                return true;
            }
            default -> {
                showHelp(sender);
                return true;
            }
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== §eArtifactOp コマンドヘルプ §6===");
        sender.sendMessage("§e/artifactop help §7- §fこのヘルプを表示");
        sender.sendMessage("§e/artifactop decomptable §7- §f分解テーブル編集GUIを開く");
        sender.sendMessage("§e/artifactop getaugment [数] §7- §fオーグメントを取得");
        sender.sendMessage("§e/artifactop reload §7- §f設定をリロード");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("artifact.admin")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subCommands = List.of("help", "decomptable", "getaugment", "reload");
            String input = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("getaugment")) {
            // 数量のサジェスト
            completions.addAll(List.of("1", "10", "64"));
        }

        return completions;
    }
}
