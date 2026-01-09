package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.artifact.gui.ArtifactEnhanceMenu;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactEquipMenu;
import io.github.itokagimaru.artifact.artifact.items.SpecialItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArtifactCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0]) {
            case "help" -> {
                showHelp(player);
                return true;
            }
            case "equip" -> {
                new ArtifactEquipMenu(player).open(player);
                return true;
            }
            case "enhance" -> {
                new ArtifactEnhanceMenu().open(player);
                return true;
            }
            case "getaugment" -> {
                // 管理者権限の確認
                if (!player.hasPermission("artifact.admin")) {
                    player.sendMessage("§cこのコマンドを実行する権限がありません");
                    return true;
                }
                ItemStack augment = SpecialItems.getAugment();
                int count = 1;
                if (args.length > 1 && !args[1].isEmpty()) {
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
                    player.getInventory().addItem(augment.clone());
                }
                player.sendMessage("§aオーグメントを" + count + "個取得しました");
                return true;
            }
        }
        return false;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== §eArtifact コマンドヘルプ §6===");
        sender.sendMessage("§e/artifact help §7- §fこのヘルプを表示します");
        sender.sendMessage("§e/artifact equip §7- §fアーティファクト装備メニューを開きます");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            list.add("equip");
            list.add("enhance");
            list.add("getaugment");
        }
        return list;
    }
}
