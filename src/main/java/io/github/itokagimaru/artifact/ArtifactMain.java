package io.github.itokagimaru.artifact;

import io.github.itokagimaru.artifact.Player.status.PlayerStatusManager;
import io.github.itokagimaru.artifact.artifact.artifacts.data.effect.EffectStack;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesFactory;
import io.github.itokagimaru.artifact.artifact.artifacts.data.series.SeriesRegistry;
import io.github.itokagimaru.artifact.command.ArtifactCommand;
import io.github.itokagimaru.artifact.command.ArtifactOpCommand;
import io.github.itokagimaru.artifact.artifact.GeneralConfig;
import io.github.itokagimaru.artifact.artifact.decompose.DecomposeConfig;
import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.AuctionScheduler;
import io.github.itokagimaru.artifact.auction.config.AuctionConfig;
import io.github.itokagimaru.artifact.auction.data.AuctionDatabase;
import io.github.itokagimaru.artifact.auction.data.AuctionRepository;
import io.github.itokagimaru.artifact.command.AuctionCommand;
import io.github.itokagimaru.artifact.command.GetNewArtifact;
import org.bukkit.Bukkit;
import io.github.itokagimaru.artifact.stash.StashCommand;
import io.github.itokagimaru.artifact.stash.StashLoginListener;
import io.github.itokagimaru.artifact.stash.StashManager;
import io.github.itokagimaru.artifact.stash.StashRepository;
import io.github.itokagimaru.artifact.utils.BaseGui;
import io.github.itokagimaru.artifact.utils.VaultAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * アーティファクトプラグインのメインクラス
 */
public final class ArtifactMain extends JavaPlugin {
    public static ArtifactMain instance;

    private AuctionConfig auctionConfig;
    private AuctionDatabase auctionDatabase;
    private AuctionRepository auctionRepository;
    private AuctionManager auctionManager;
    private AuctionScheduler auctionScheduler;
    private VaultAPI vaultAPI;

    SeriesRegistry seriesRegistry;
    EffectStack effectStack;

    PlayerStatusManager playerStatusManager;

    private StashManager stashManager;
    private VaultAPI vaultAPI;
    private GeneralConfig generalConfig;
    private DecomposeConfig decomposeConfig;
    public static JavaPlugin plugin;
    private static ArtifactMain instance;

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

        plugin = this;
        instance = this;

        // VaultAPI初期化
        vaultAPI = new VaultAPI(this);
        generalConfig = new GeneralConfig(this);
        decomposeConfig = new DecomposeConfig(this);

        // オークションシステム初期化
        initAuctionSystem();

        // Stashシステム初期化
        initStashSystem();

        // GUI初期化
        BaseGui.setup(this);
        getServer().getPluginManager().registerEvents(new BaseGui.GuiListener(), this);

        // コマンド登録
        GetNewArtifact getNewArtifact = new GetNewArtifact();
        registerCommandWithTabCompleter("getNewArtifact", getNewArtifact, getNewArtifact);

        // アーティファクトコマンド登録
        ArtifactCommand artifactCommand = new ArtifactCommand();
        registerCommandWithTabCompleter("artifact", artifactCommand, artifactCommand);

        // アーティファクト管理者コマンド登録
        ArtifactOpCommand artifactOpCommand = new ArtifactOpCommand();
        registerCommandWithTabCompleter("artifactop", artifactOpCommand, artifactOpCommand);
        
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

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            getLogger().severe("MythicMobs not found!");
            getServer().getPluginManager().disablePlugin(this);
        } else{
            getLogger().info("MythicMobs hooked!");
        }

        // artifactSeriesの読み込み
        loadArtifactFiles();


        instance = this;
        getSLF4JLogger().info("Enabled Artifact plugin");

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

            getSLF4JLogger().info("Initialized Auction database");

        } catch (SQLException e) {
            getSLF4JLogger().error("Failed to initialize Auction database: " + e.getMessage());
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

            getSLF4JLogger().info("Initialized Stash system");

        } catch (SQLException e) {
            getSLF4JLogger().error("Failed to initialize Stash system: " + e.getMessage());
            e.printStackTrace();
        }
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

    public static void reloadConfigs() {
        getInstance().generalConfig.reload();
        getInstance().auctionConfig.reload();
        getInstance().decomposeConfig.reload();
    }

    public static ArtifactMain getInstance() {
        return instance;
    }
    private void loadArtifactFiles() {

        File pluginsDir = getDataFolder().getParentFile();
        File artifactDir = new File(pluginsDir, "artifact/series");

        if (!artifactDir.exists()) {
            artifactDir.mkdirs();
            getLogger().info("artifact ディレクトリを作成しました");
            loadArtifactFiles();
            return;
        }

        File[] ymlFiles = artifactDir.listFiles(
                (dir, name) -> name.endsWith(".yml")
        );

        if (ymlFiles == null || ymlFiles.length == 0) {
            getLogger().info("series ディレクトリに yml がありません");
            return;
        }
        seriesRegistry = new SeriesRegistry();
        for (File file : ymlFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            try{
                seriesRegistry.addSeries(SeriesFactory.makeSeries(config));
                String fileName = file.getName();
                getLogger().info("Loading artifact file: " + fileName);
            } catch (Exception e){
                getSLF4JLogger().error("SeriesFile:" + file.getName() +"の読み込みに失敗しました");
            }


        }
    }



    // ========== Getter ==========

    public static AuctionManager getAuctionManager() {
        return getInstance().auctionManager;
    }

    public static StashManager getStashManager() {
        return getInstance().stashManager;
    }

    public static GeneralConfig getGeneralConfig() {
        return getInstance().generalConfig;
    }

    public static VaultAPI getVaultAPI() {
        return getInstance().vaultAPI;
    }

    public static DecomposeConfig getDecomposeConfig() {
        return getInstance().decomposeConfig;
    }
}
