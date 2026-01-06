package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.artifact.artifacts.factory.Factory;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tire.Tier;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetNewArtifact implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this Command");
            return false;
        }

        if (args.length != 3) {
            player.sendMessage(Component.text("引数の数に異常があります"));
            player.sendMessage(Component.text("/getnewartifact <seriesId> <slotId> <tier>"));
            return false;
        }
        int[] argsInt = new int[3];
        for (int i = 0; i < args.length; i++){
            try {
                argsInt[i] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("引数はint型で入力してください"));
                player.sendMessage(Component.text("/getnewartifact <seriesId> <slotId> <tier>"));
                return false;
            }
        }

        Factory factory = new Factory();
        if(Series.artifactSeres.fromId(argsInt[0]) == null){
            player.sendMessage(Component.text("そのseriesIdは不正です seriesId:" + argsInt[0]));
            player.sendMessage(Component.text("--------<seriesIdList>--------"));
            for (Series.artifactSeres seres : Series.artifactSeres.values()){
                player.sendMessage(Component.text(seres.getId + ":" + seres.getArtifactType().getSeriesName()));
            }
            player.sendMessage(Component.text(""));
        }
        if(Slot.artifactSlot.fromId(argsInt[1]) == null){
            player.sendMessage(Component.text("そのslotIdは不正です slotId:" + argsInt[1]));
            player.sendMessage(Component.text("--------<slotIdList>--------"));
            for (Slot.artifactSlot slot : Slot.artifactSlot.values()){
                player.sendMessage(Component.text(slot.getId + ":" + slot.getSlotName));
            }
            player.sendMessage(Component.text(""));
        }
        if(Tier.artifactTier.fromId(argsInt[2]) == null){
            player.sendMessage(Component.text("そのtierは不正です tier:" + argsInt[2]));
            player.sendMessage(Component.text("--------<tierList>--------"));
            for (Tier.artifactTier tier : Tier.artifactTier.values()){
                player.sendMessage(Component.text(tier.getId + ":" + tier.getText));
            }
            player.sendMessage(Component.text(""));
        }
        if (Series.artifactSeres.fromId(argsInt[0]) == null || Slot.artifactSlot.fromId(argsInt[1]) == null || Tier.artifactTier.fromId(argsInt[2]) == null) return false;
        player.give(factory.makeNewArtifact(Series.artifactSeres.fromId(argsInt[0]), Slot.artifactSlot.fromId(argsInt[1]), Tier.artifactTier.fromId(argsInt[2])));
        return true;
    }
}
