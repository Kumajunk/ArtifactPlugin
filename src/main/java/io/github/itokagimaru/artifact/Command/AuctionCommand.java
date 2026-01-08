package io.github.itokagimaru.artifact.Command;

import io.github.itokagimaru.artifact.auction.AuctionManager;
import io.github.itokagimaru.artifact.auction.Result;
import io.github.itokagimaru.artifact.auction.gui.AuctionMainMenu;
import io.github.itokagimaru.artifact.auction.gui.AuctionMyListingsMenu;
import io.github.itokagimaru.artifact.auction.gui.AuctionSearchMenu;
import io.github.itokagimaru.artifact.auction.gui.AuctionSellMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * オークションコマンドを処理するクラス
 * /auction - メインメニュー表示
 * /auction search - 検索画面
 * /auction sell - 出品画面
 * /auction my - 自分の出品一覧
 * /auction cancel <id> - 出品キャンセル
 */
public class AuctionCommand implements CommandExecutor, TabCompleter {

    private AuctionManager manager;

    /**
     * AuctionManagerを設定する
     * プラグイン初期化時に呼び出される
     * 
     * @param manager オークションマネージャー
     */
    public void setManager(AuctionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // プレイヤーのみ使用可能
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます");
            return true;
        }

        // マネージャーが未設定の場合
        if (manager == null) {
            player.sendMessage("§cオークションシステムが初期化されていません");
            return true;
        }

        if (!manager.getAllowedWorlds().contains(player.getWorld().getName()) && !player.hasPermission("artifact.admin")) {
            player.sendMessage("§cこのワールドではオークションコマンドを使用できません");
            return true;
        }

        // サブコマンドなし → メインメニュー
        if (args.length == 0) {
            new AuctionMainMenu(manager).open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "search" -> new AuctionSearchMenu(manager).open(player);
            case "sell" -> new AuctionSellMenu(manager).open(player);
            case "my", "mine", "list" -> new AuctionMyListingsMenu(manager).open(player);
            case "cancel" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用方法: /auction cancel <出品ID>");
                    return true;
                }
                handleCancel(player, args[1]);
            }
            case "help" -> sendHelp(player);
            case "reload" -> {
                if (player.hasPermission("artifact.admin")) {
                    player.sendMessage("§a設定をリロードしました");
                    manager.getConfig().reload();
                    manager.getConfig().save();
                } else {
                    player.sendMessage("§cこのコマンドを使用する権限がありません");
                }
            }
            default -> player.sendMessage("§c不明なサブコマンドです。/auction help でヘルプを表示");
        }

        return true;
    }

    /**
     * 出品キャンセルを処理
     */
    private void handleCancel(Player player, String idString) {
        try {
            UUID listingId = UUID.fromString(idString);
            Result<Void> result = manager.cancelListing(player, listingId);
            
            if (result.isSuccess()) {
                player.sendMessage("§a出品をキャンセルしました");
            } else {
                player.sendMessage("§c" + result.getErrorMessage());
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c無効な出品IDです");
        }
    }

    /**
     * ヘルプメッセージを送信
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== アーティファクト・オークション ===");
        player.sendMessage("§e/auction §7- メインメニューを開く");
        player.sendMessage("§e/auction search §7- 検索画面を開く");
        player.sendMessage("§e/auction sell §7- 出品画面を開く");
        player.sendMessage("§e/auction my §7- 自分の出品一覧");
        player.sendMessage("§e/auction cancel <ID> §7- 出品をキャンセル");
        player.sendMessage("§e/auction help §7- このヘルプを表示");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("search", "sell", "my", "cancel", "help");
            return subCommands.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("cancel")) {
            // 自分の出品IDを補完
            if (sender instanceof Player player && manager != null) {
                return manager.getPlayerListings(player.getUniqueId()).stream()
                    .map(l -> l.getListingId().toString())
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}
