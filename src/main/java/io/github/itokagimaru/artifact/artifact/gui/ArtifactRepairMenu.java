package io.github.itokagimaru.artifact.artifact.gui;

import io.github.itokagimaru.artifact.ArtifactMain;
import io.github.itokagimaru.artifact.artifact.artifacts.artifact.BaseArtifact;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ArtifactToItem;
import io.github.itokagimaru.artifact.artifact.artifacts.factory.ItemToArtifact;
import io.github.itokagimaru.artifact.utils.BaseGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public class ArtifactRepairMenu extends BaseGui {
    private static final int GUI_SIZE = 54;
    private static final int BASE_SLOT = 13;
    private static final int EXECUTE_BUTTON = 40;
    private final ItemStack repairTarget;

    public ArtifactRepairMenu() {
        this(null);
    }

    public ArtifactRepairMenu(ItemStack repairTarget) {
        super(GUI_SIZE, "§lアーティファクト修理");
        this.repairTarget = repairTarget;
        setupGui();
        setupPlayerInventoryHandler();
    }

    private void setupPlayerInventoryHandler() {
        setPlayerInventoryClickHandler((player, slot, item, clickType) -> {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            if (!ItemToArtifact.isArtifact(item)) {
                player.sendMessage("§cこれはアーティファクトではありません!");
                return;
            }
            if (repairTarget == null) {
                BaseArtifact artifact = ItemToArtifact.convert(item).orElse(null);
                if (artifact == null) {
                    player.sendMessage("§cアーティファクトの情報が取得できませんでした!");
                    return;
                }
                
                if (artifact.getDurability() >= artifact.getMaxDurability()) {
                    player.sendMessage("§cこのアーティファクトはすでに耐久値が最大です!");
                    return;
                }
                
                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                new ArtifactRepairMenu(item).open(player);
            }
        });
    }

    private void setupGui() {
        fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(" "));

        int cost = 0;
        if (repairTarget != null) {
            Optional<BaseArtifact> artifactOpt = ItemToArtifact.convert(repairTarget);
            if (artifactOpt.isPresent()) {
                BaseArtifact artifact = artifactOpt.get();
                int maxDurability = artifact.getMaxDurability();
                int currentDurability = artifact.getDurability();
                
                if (currentDurability < maxDurability) {
                    double penaltyRate = ArtifactMain.getGeneralConfig().getRepairPenaltyRate();
                    int rawRecovered = maxDurability - currentDurability;
                    int penalty = (int) Math.ceil(rawRecovered * penaltyRate);
                    
                    // 実際に増える耐久値 = (以前の最大 - ペナルティ) - 現在
                    int actualRecovered = Math.max(0, (maxDurability - penalty) - currentDurability);
                    
                    if (actualRecovered > 0) {
                        cost = (int) Math.round((25 * Math.pow(1.12, artifact.getLv())) * actualRecovered);
                    }
                }
            }
        }

        final int finalCost = cost;
        if (repairTarget != null) {
            setReturnItem(BASE_SLOT, repairTarget, player -> {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(repairTarget);
                if (!remaining.isEmpty()) {
                    remaining.values().forEach(i ->
                            player.getWorld().dropItemNaturally(player.getLocation(), i)
                    );
                }
                new ArtifactRepairMenu(null).open(player);
            });
        } else {
            setItem(BASE_SLOT, new ItemBuilder()
                    .setMaterial(Material.BARRIER)
                    .setName("§c修理するアーティファクトをセットしてください"));
        }

        // 実行ボタンの設置
        setItem(EXECUTE_BUTTON, new ItemBuilder()
                .setMaterial(Material.ANVIL)
                .setName("§a修理を実行する")
                .addLore("§7耐久値を最大まで回復させますが、")
                .addLore("§7回復量に応じて最大耐久値が減少します。")
                .addLore(" ")
                .addLore("§7修理費用: §e" + finalCost+ "ゴールド")
                .setClickAction(ClickType.LEFT, player -> {
                    if (repairTarget == null) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§c修理するアーティファクトがセットされていません!");
                        return;
                    }
                    
                    Optional<BaseArtifact> artifactOpt = ItemToArtifact.convert(repairTarget);
                    if (artifactOpt.isEmpty()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§cアーティファクトの情報が取得できませんでした!");
                        return;
                    }
                    
                    BaseArtifact baseArtifact = artifactOpt.get();
                    int maxDurability = baseArtifact.getMaxDurability();
                    int currentDurability = baseArtifact.getDurability();
                    
                    if (currentDurability >= maxDurability) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§cこのアーティファクトはすでに耐久値が最大です!");
                        return;
                    }

                    // 所持金チェック
                    if (ArtifactMain.getVaultAPI().getBalance(player.getUniqueId()) < finalCost) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage("§c修理費用が足りません! (必要: §e" + finalCost + "ゴールド§c)");
                        return;
                    }
                    
                    double penaltyRate = ArtifactMain.getGeneralConfig().getRepairPenaltyRate();
                    int recoveredAmount = maxDurability - currentDurability;
                    int penalty = (int) Math.ceil(recoveredAmount * penaltyRate);
                    
                    // 新しい最大耐久値を計算（0未満にはしない）
                    int newMaxDurability = Math.max(1, maxDurability - penalty);

                    // 所持金を差し引く
                    if (!ArtifactMain.getVaultAPI().withdraw(player.getUniqueId(), finalCost)) {
                        player.sendMessage("§c支払いに失敗しました。");
                        return;
                    }
                    
                    // 耐久を回復させ、最大耐久値を更新
                    baseArtifact.setMaxDurability(newMaxDurability);
                    baseArtifact.setDurability(newMaxDurability);
                    
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 0.6f);
                    player.sendMessage("§aアーティファクトを修理しました!(最大耐久値 -" + penalty + ")");
                    
                    ItemStack repairedItem = ArtifactToItem.convert(baseArtifact);
                    new ArtifactRepairMenu(repairedItem).open(player);
                })
        );
    }
}
