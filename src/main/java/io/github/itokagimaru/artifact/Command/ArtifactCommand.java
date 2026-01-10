package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactDecomposeMenu;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactEnhanceMenu;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactEquipMenu;
import io.github.itokagimaru.artifact.artifact.gui.ArtifactSellMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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

        if (!ArtifactMain.getAuctionManager().getAllowedWorlds().contains(player.getWorld().getName()) && !player.hasPermission("artifact.admin")) {
            player.sendMessage("§cこのワールドではオークションコマンドを使用できません");
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
            case "sell" -> {
                new ArtifactSellMenu().open(player);
                return true;
            }
            case "decompose" -> {
                new ArtifactDecomposeMenu().open(player);
                return true;
            }
        }
        return false;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== §eArtifact コマンドヘルプ §6===");
        sender.sendMessage("§e/artifact help §7- §fこのヘルプを表示します");
        sender.sendMessage("§e/artifact equip §7- §fアーティファクト装備メニューを開きます");
        sender.sendMessage("§e/artifact enhance §7- §fアーティファクト強化メニューを開きます");
        sender.sendMessage("§e/artifact sell §7- §fアーティファクト売却メニューを開きます");
        sender.sendMessage("§e/artifact decompose §7- §fアーティファクト分解メニューを開きます");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            list.add("equip");
            list.add("enhance");
            list.add("sell");
            list.add("decompose");
        }
        return list;
    }
}
