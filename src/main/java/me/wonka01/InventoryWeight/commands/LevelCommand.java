package me.wonka01.InventoryWeight.commands;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.playerweight.PlayerLevelManager;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelCommand implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        InventoryWeight plugin = InventoryWeight.getPlugin(InventoryWeight.class);
        PlayerLevelManager manager = plugin.getLevelManager();

        if (manager == null || !manager.isEnabled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getLevelingDisabledMessage()));
            return;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        LanguageConfig.getConfig().getMessages().getInvalidPlayer()));
                return;
            }
            Player player = (Player) sender;
            respondWithLevelInfo(sender, plugin, manager, player, player.getUniqueId());
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidCommand()));
            return;
        }

        String action = args[1].toLowerCase();
        String targetName = args[2];
        if (!targetName.matches("[A-Za-z0-9_]+")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidPlayer()));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetId = target.getUniqueId();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidCommand()));
            return;
        }

        double baseWeightLimit = plugin.getConfig().getInt("weightLimit");
        if (target.isOnline()) {
            baseWeightLimit = plugin.getBaseWeightLimit(target.getPlayer());
        }

        if (action.equals("set")) {
            if (!sender.hasPermission("inventoryweight.level.set")) {
                sender.sendMessage(LanguageConfig.getConfig().getMessages().getNoPermission());
                return;
            }
            manager.setLevel(targetId, amount, baseWeightLimit);
            updateOnlinePlayerWeight(plugin, manager, targetId, baseWeightLimit);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getLevelSet()
                            .replace("%player%", targetName)
                            .replace("%level%", String.valueOf(manager.getLevel(targetId)))));
        } else if (action.equals("add")) {
            if (!sender.hasPermission("inventoryweight.level.add")) {
                sender.sendMessage(LanguageConfig.getConfig().getMessages().getNoPermission());
                return;
            }
            manager.addLevel(targetId, amount, baseWeightLimit);
            updateOnlinePlayerWeight(plugin, manager, targetId, baseWeightLimit);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getLevelAdd()
                            .replace("%player%", targetName)
                            .replace("%level%", String.valueOf(manager.getLevel(targetId)))));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidCommand()));
        }
    }

    private void updateOnlinePlayerWeight(InventoryWeight plugin, PlayerLevelManager manager, UUID playerId,
            double baseWeightLimit) {
        if (!PlayerWeightMap.getPlayerWeightMap().containsKey(playerId)) {
            return;
        }
        PlayerWeight playerWeight = PlayerWeightMap.getPlayerWeightMap().get(playerId);
        double effective = manager.getEffectiveMaxWeight(playerId, baseWeightLimit);
        int level = manager.getLevel(playerId);
        double multiplier = manager.getEffectiveMultiplier(playerId, baseWeightLimit);
        playerWeight.applyLevelData(baseWeightLimit, level, multiplier, effective);
        playerWeight.changeSpeed();
    }

    private void respondWithLevelInfo(CommandSender sender, InventoryWeight plugin, PlayerLevelManager manager,
            Player player, UUID playerId) {
        double baseWeightLimit = plugin.getBaseWeightLimit(player);
        double effective = manager.getEffectiveMaxWeight(playerId, baseWeightLimit);
        int level = manager.getLevel(playerId);
        double multiplier = manager.getEffectiveMultiplier(playerId, baseWeightLimit);

        String message = LanguageConfig.getConfig().getMessages().getLevelInfo()
                .replace("%level%", String.valueOf(level))
                .replace("%multiplier%", String.valueOf(multiplier))
                .replace("%max%", String.valueOf(effective));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
