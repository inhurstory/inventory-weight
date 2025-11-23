package me.wonka01.InventoryWeight.util;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wonka01.InventoryWeight.playerweight.PlayerLevelManager;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
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
        return "inventoryweight";
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

        if (identifier.equals("speed")) {
            if (player.isOnline()) {
                Player onlinePlayer = (Player) player;
                float speed = onlinePlayer.getWalkSpeed();
                return String.valueOf(speed);
            }
            return "0.0";
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
                return String.valueOf(levelManager.getMultiplierPerLevel());
            }
            return "0";
        }
        return null;
    }
}
