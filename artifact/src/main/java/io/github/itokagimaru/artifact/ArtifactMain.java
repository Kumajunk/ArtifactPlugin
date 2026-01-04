package io.github.itokagimaru.artifact;

import io.github.itokagimaru.artifact.Command.GetNewArtifact;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArtifactMain extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommand("getNewArtifact", new GetNewArtifact());
        getSLF4JLogger().info("コマンドを登録しました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new RuntimeException(String.format("コマンド %s が見つかりませんでした。", name));
        command.setExecutor(executor);
    }
}
