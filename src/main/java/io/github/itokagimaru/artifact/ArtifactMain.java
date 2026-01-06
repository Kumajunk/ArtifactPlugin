package io.github.itokagimaru.artifact;

import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.AuctionScheduler;
import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import io.github.itokagimaru.artifact.auction.data.AuctionDatabase;
import io.github.itokagimaru.artifact.auction.data.AuctionRepository;
import io.github.itokagimaru.artifact.Command.AuctionCommand;
import io.github.itokagimaru.artifact.Command.GetNewArtifact;
import io.github.itokagimaru.artifact.stash.StashCommand;
import io.github.itokagimaru.artifact.stash.StashLoginListener;
import io.github.itokagimaru.artifact.stash.StashManager;
import io.github.itokagimaru.artifact.stash.StashRepository;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

/**
 * アーティファクトプラグインのメインクラス
 */
public final class ArtifactMain extends JavaPlugin {

    private AuctionConfig auctionConfig;
    private AuctionDatabase auctionDatabase;
    private AuctionRepository auctionRepository;
    private AuctionManager auctionManager;
    private AuctionScheduler auctionScheduler;
    private VaultAPI vaultAPI;

    private StashManager stashManager;

    @Override
    public void onEnable() {
        // データフォルダ作成
        if (!getDataFolder().exists()) {
            boolean isSuccess = getDataFolder().mkdirs();
            if (isSuccess) {
                getSLF4JLogger().info("データフォルダを作成しました: {}", getDataFolder().getAbsolutePath());
            } else {
                getSLF4JLogger().warn("データフォルダの作成に失敗しました: {}", getDataFolder().getAbsolutePath());
            }
        }

        // VaultAPI初期化
        vaultAPI = new VaultAPI(this);

        // オークションシステム初期化
        initAuctionSystem();

        // Stashシステム初期化
        initStashSystem();

        // GUI初期化
        BaseGui.setup(this);
        getServer().getPluginManager().registerEvents(new BaseGui.GuiListener(), this);

        // コマンド登録
        registerCommand("getNewArtifact", new GetNewArtifact());
        
        // オークションコマンド登録
        AuctionCommand auctionCommand = new AuctionCommand();
        auctionCommand.setManager(auctionManager);
        registerCommandWithTabCompleter("auction", auctionCommand, auctionCommand);

        // Stashコマンド登録
        StashCommand stashCommand = new StashCommand();
        stashCommand.setStashManager(stashManager);
        registerCommandWithTabCompleter("stash", stashCommand, stashCommand);

        // Stashログイン通知
        StashLoginListener stashLoginListener = new StashLoginListener();
        stashLoginListener.setStashManager(stashManager);
        getServer().getPluginManager().registerEvents(stashLoginListener, this);

        getSLF4JLogger().info("アーティファクトプラグインを有効化しました");
    }

    @Override
    public void onDisable() {
        // スケジューラー停止
        if (auctionScheduler != null) {
            auctionScheduler.stop();
        }

        // データベース接続クローズ
        if (auctionDatabase != null) {
            auctionDatabase.close();
        }

        getSLF4JLogger().info("アーティファクトプラグインを無効化しました");
    }

    /**
     * オークションシステムを初期化する
     */
    private void initAuctionSystem() {
        try {
            // 設定読み込み
            auctionConfig = new AuctionConfig(this);

            // データベース初期化
            auctionDatabase = new AuctionDatabase(this, auctionConfig);
            auctionDatabase.connect();
            auctionDatabase.initTables();

            // リポジトリ初期化
            auctionRepository = new AuctionRepository(this, auctionDatabase);

            getSLF4JLogger().info("オークションデータベースを初期化しました");

        } catch (SQLException e) {
            getSLF4JLogger().error("オークションシステムの初期化に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stashシステムを初期化する
     */
    private void initStashSystem() {
        try {
            // Stashリポジトリ初期化
            // Stashシステム
            StashRepository stashRepository = new StashRepository(this, auctionDatabase);
            stashRepository.initTable();

            // Stashマネージャー初期化
            stashManager = new StashManager(this, stashRepository, vaultAPI);

            // AuctionManagerとScheduler初期化（Stash連携後）
            auctionManager = new AuctionManager(this, auctionRepository, auctionConfig, vaultAPI, stashManager);

            // スケジューラー開始
            auctionScheduler = new AuctionScheduler(this, auctionRepository, auctionConfig, vaultAPI, auctionManager, stashManager);
            auctionScheduler.start();

            getSLF4JLogger().info("Stashシステムを初期化しました");

        } catch (SQLException e) {
            getSLF4JLogger().error("Stashシステムの初期化に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * コマンドを登録する
     */
    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getSLF4JLogger().warn("コマンド {} が見つかりませんでした", name);
            return;
        }
        command.setExecutor(executor);
    }

    /**
     * コマンドとTabCompleterを登録する
     */
    private void registerCommandWithTabCompleter(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getSLF4JLogger().warn("コマンド {} が見つかりませんでした", name);
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(tabCompleter);
    }

    // ========== Getter ==========

//    public AuctionManager getAuctionManager() {
//        return auctionManager;
//    }
//
//    public AuctionConfig getAuctionConfig() {
//        return auctionConfig;
//    }
//
//    public VaultAPI getVaultAPI() {
//        return vaultAPI;
//    }
//
//    public StashManager getStashManager() {
//        return stashManager;
//    }
}
