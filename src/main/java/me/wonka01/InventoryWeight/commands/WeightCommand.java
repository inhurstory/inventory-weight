package me.wonka01.InventoryWeight.commands;

import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import me.wonka01.InventoryWeight.util.WorldList;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class WeightCommand implements SubCommand {
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "You must be a player to use this command"));
            return;
        }

        Player player = (Player) sender;
        WorldList worldList = WorldList.getInstance();
        if (player.hasPermission("inventoryweight.off") || player.getGameMode().equals(GameMode.CREATIVE)
                || !(worldList.isInventoryWeightEnabled(player.getWorld().getName()))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getNoPermission()));
            return;
        }
        PlayerWeight playerWeight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00"); // setting the format
        String roundedWeight = decimalFormatter.format(playerWeight.getWeight());
        String roundedMaxWeight = decimalFormatter.format(playerWeight.getMaxWeight());

        player.sendMessage(
                ChatColor.translateAlternateColorCodes('&', LanguageConfig.getConfig().getMessages().getWeight()
                        + ": &a" + roundedWeight + " &f / &c"
                        + roundedMaxWeight));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                LanguageConfig.getConfig().getMessages().getSpeed() + ": &a" + playerWeight.getPercentage() + "%"));
        player.sendMessage(ChatColor.WHITE + "[" + playerWeight.getSpeedDisplay() + ChatColor.WHITE + "]");
    }
}
