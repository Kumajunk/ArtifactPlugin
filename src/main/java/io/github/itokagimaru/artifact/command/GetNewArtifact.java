package io.github.itokagimaru.artifact.command;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.Factory;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tier.Tier;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GetNewArtifact implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean error = false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command");
            return false;
        }

        if (args.length != 3) {
            player.sendMessage(Component.text("引数の数に異常があります"));
            player.sendMessage(Component.text("/getnewartifact <seriesKey> <slotId> <tier>"));
            return false;
        }
        int[] argsInt = new int[2];
        for (int i = 1; i < args.length; i++){
            try {
                argsInt[i-1] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("引数はint型で入力してください"));
                player.sendMessage(Component.text("/getnewartifact <seriesKey> <slotId> <tier>"));
                return false;
            }
        }

        Factory factory = new Factory();
        if(SeriesRegistry.getSeries(args[0]) == null){
            player.sendMessage(Component.text("そのseriesIdは不正です seriesId:" + argsInt[0]));
            player.sendMessage(Component.text("--------<seriesKeyList>--------"));
            for (Series seres : SeriesRegistry.seriesRegistry.values()) {
                player.sendMessage(Component.text(seres.getSeriesName()));
            }
            player.sendMessage(Component.text(""));
            error = true;
        }
        if(Slot.artifactSlot.fromId(argsInt[0]) == null){
            player.sendMessage(Component.text("そのslotIdは不正です slotId:" + argsInt[1]));
            player.sendMessage(Component.text("--------<slotIdList>--------"));
            for (Slot.artifactSlot slot : Slot.artifactSlot.values()){
                player.sendMessage(Component.text(slot.getId + ":" + slot.getSlotName));
            }
            player.sendMessage(Component.text(""));
            error = true;
        }
        if(Tier.artifactTier.fromId(argsInt[1]) == null){
            player.sendMessage(Component.text("そのtierは不正です tier:" + argsInt[1]));
            player.sendMessage(Component.text("--------<tierList>--------"));
            for (Tier.artifactTier tier : Tier.artifactTier.values()){
                player.sendMessage(Component.text(tier.getId + ":" + tier.getText));
            }
            player.sendMessage(Component.text(""));
            error = true;
        }
        if(error) return false;
        player.give(factory.makeNewArtifact(SeriesRegistry.getSeries(args[0]), Slot.artifactSlot.fromId(argsInt[0]), Tier.artifactTier.fromId(argsInt[1])));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length){
            case 1 -> {
                for (Series.artifactSeres seres : Series.artifactSeres.values()){
                    list.add(String.valueOf(seres.getId));
                }
            }
            case 2 -> {
                for (Slot.artifactSlot slot : Slot.artifactSlot.values()){
                    list.add(String.valueOf(slot.getId));
                }
            }
            case 3 -> {
                for (Tier.artifactTier tier : Tier.artifactTier.values()) {
                    list.add(String.valueOf(tier.getId));
                }
            }
        }
        return list;
    }
}
