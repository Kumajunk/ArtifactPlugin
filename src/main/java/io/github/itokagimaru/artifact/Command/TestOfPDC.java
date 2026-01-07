package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.artifact.artifacts.data.series.Series;
import io.github.itokagimaru.artifact.artifact.artifacts.data.slot.Slot;
import io.github.itokagimaru.artifact.artifact.artifacts.data.tire.Tier;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.Factory;
import io.github.itokagimaru.artifact.data.EntityData;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

public class TestOfPDC implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(Component.text("引数の数に異常があります"));
            player.sendMessage(Component.text("/testofphase <UUID>"));
            return false;
        }

        World world = player.getWorld();
        for (Entity mob : world.getEntities()){
            if (args[0].equals(String.valueOf(mob.getUniqueId()))){
                int phase = EntityData.PHASE.get(mob);
                player.sendMessage(Component.text(phase));
            }
        }
        return true;
    }


}
