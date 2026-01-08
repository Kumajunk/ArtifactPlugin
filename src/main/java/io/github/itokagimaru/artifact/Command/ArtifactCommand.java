package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.artifact.gui.ArtifactEquipMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
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
        }
        return false;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== §eArtifact コマンドヘルプ §6===");
        sender.sendMessage("§e/artifact help §7- §fこのヘルプを表示します");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
