package me.wonka01.InventoryWeight.commands;

import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import me.wonka01.InventoryWeight.util.WorldList;
import me.wonka01.InventoryWeight.util.WorldList.TempForceState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempForceCommand implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("inventoryweight.tempforce")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getNoPermission()));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidCommand()));
            return;
        }

        String action = args[1].toLowerCase();
        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidPlayer()));
            return;
        }

        WorldList worldList = WorldList.getInstance();
        if (action.equals("on")) {
            worldList.setTempForce(target.getUniqueId(), TempForceState.ON);
            applyChange(target);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getTempForceOn()
                            .replace("%player%", target.getName())));
        } else if (action.equals("off")) {
            worldList.setTempForce(target.getUniqueId(), TempForceState.OFF);
            applyChange(target);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getTempForceOff()
                            .replace("%player%", target.getName())));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getInvalidCommand()));
        }
    }

    private void applyChange(Player target) {
        PlayerWeight playerWeight = PlayerWeightMap.getPlayerWeightMap().get(target.getUniqueId());
        if (playerWeight != null) {
            playerWeight.changeSpeed();
        }
    }
}
