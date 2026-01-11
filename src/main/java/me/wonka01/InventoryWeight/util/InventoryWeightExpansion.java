package me.wonka01.InventoryWeight.util;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wonka01.InventoryWeight.playerweight.PlayerLevelManager;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class InventoryWeightExpansion extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "wonka01";
    }

    @Override
    public String getIdentifier() {
        return "iw";
    }

    @Override
    public String getVersion() {
        return "2.17";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        InventoryWeight plugin = InventoryWeight.getPlugin(InventoryWeight.class);
        PlayerLevelManager levelManager = plugin.getLevelManager();

        if (identifier.equals("weight")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                DecimalFormat decimalFormatter = new DecimalFormat("#0.00"); // setting the format

                return decimalFormatter.format(weight.getWeight());
            }
            return "0";
        }

        if (identifier.equals("maxweight")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                DecimalFormat decimalFormatter = new DecimalFormat("#0.00"); // setting the format
                return decimalFormatter.format(weight.getMaxWeight());
            }
            return String.valueOf(PlayerWeight.defaultMaxCapacity);
        }

        if (identifier.equals("weight_line")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                DecimalFormat df = new DecimalFormat("#0.00");
                String roundedWeight = df.format(weight.getWeight());
                String roundedMaxWeight = df.format(weight.getMaxWeight());
                return ChatColor.translateAlternateColorCodes('&',
                        plugin.getLanguageConfig().getMessages().getWeight() + ": &a" + roundedWeight + " &f/ &c"
                                + roundedMaxWeight);
            }
            return "";
        }

        if (identifier.equals("speed")) {
            if (player.isOnline()) {
                Player onlinePlayer = (Player) player;
                float speed = onlinePlayer.getWalkSpeed();
                return String.valueOf(speed);
            }
            return "0.0";
        }

        if (identifier.equals("speed_line")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                return ChatColor.translateAlternateColorCodes('&',
                        plugin.getLanguageConfig().getMessages().getSpeed() + ": &a" + weight.getPercentage() + "%");
            }
            return "";
        }

        if (identifier.equals("speed_bar")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                return ChatColor.WHITE + "[" + weight.getSpeedDisplay() + ChatColor.WHITE + "]";
            }
            return "";
        }

        if (identifier.equals("weight_full")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                // Combine lines similar to /iw weight output, separated by new lines
                String weightLine = onRequest(player, "weight_line");
                String speedLine = onRequest(player, "speed_line");
                String barLine = onRequest(player, "speed_bar");
                return weightLine + "\n" + speedLine + "\n" + barLine;
            }
            return "";
        }

        if (identifier.equals("weightPercentage")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                double weightPercentage = (weight.getWeight() / weight.getMaxWeight()) * 100;
                DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
                return decimalFormatter.format(weightPercentage) + "%";
            }
            return "0.0%";
        }

        if (identifier.equals("inventorybar")) {
            if (PlayerWeightMap.getPlayerWeightMap().containsKey(player.getUniqueId())) {
                PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(player.getUniqueId());
                return weight.getSpeedDisplay();
            }
            return "";
        }
        if (identifier.equals("level")) {
            if (levelManager != null && levelManager.isEnabled()) {
                return String.valueOf(levelManager.getLevel(player.getUniqueId()));
            }
            return "0";
        }
        if (identifier.equals("multiplier")) {
            if (levelManager != null && levelManager.isEnabled()) {
                double baseWeightLimit = plugin.getConfig().getInt("weightLimit");
                if (player.isOnline()) {
                    Player onlinePlayer = (Player) player;
                    baseWeightLimit = plugin.getBaseWeightLimit(onlinePlayer);
                }
                double multiplier = levelManager.getEffectiveMultiplier(player.getUniqueId(), baseWeightLimit);
                return String.valueOf(multiplier);
            }
            return "0";
        }
        return null;
    }
}
