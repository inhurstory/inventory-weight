package me.wonka01.InventoryWeight.commands;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.playerweight.LevelScalingStrategy;
import me.wonka01.InventoryWeight.playerweight.PlayerLevelManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PreviewCommand implements SubCommand {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        InventoryWeight plugin = InventoryWeight.getPlugin(InventoryWeight.class);
        PlayerLevelManager manager = plugin.getLevelManager();

        if (!sender.hasPermission("inventoryweight.preview")) {
            sender.sendMessage(LanguageConfig.getConfig().getMessages().getNoPermission());
            return;
        }

        if (manager == null || !manager.isEnabled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getLevelingDisabledMessage()));
            return;
        }

        LevelScalingStrategy strategy = manager.getScalingStrategy();
        int maxLevel = plugin.getConfig().getInt("leveling.maxLevel", 1);
        if (maxLevel < 1) {
            maxLevel = 1;
        }

        // 以全域設定的 weightLimit 為基礎做預覽
        double baseWeightLimit = plugin.getConfig().getInt("weightLimit");

        sender.sendMessage(ChatColor.GOLD + "[InventoryWeight] 等級負重預覽 (1 - " + maxLevel + ")");
        sender.sendMessage(ChatColor.YELLOW + "模式: " + strategy.getMode().name().toLowerCase());
        for (int level = 1; level <= maxLevel; level++) {
            double multiplier = strategy.computeMultiplier(level);
            double capacity = strategy.computeEffectiveWeight(baseWeightLimit, level);
            sender.sendMessage(ChatColor.GRAY + "Lv " + level + ChatColor.YELLOW + " | 倍率: "
                    + DECIMAL_FORMAT.format(multiplier) + " | 負重: " + DECIMAL_FORMAT.format(capacity));
        }
    }
}
