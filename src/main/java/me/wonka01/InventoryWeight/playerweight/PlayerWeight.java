package me.wonka01.InventoryWeight.playerweight;

import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.events.FreezePlayerEvent;
import me.wonka01.InventoryWeight.events.PreventJumpEvent;
import me.wonka01.InventoryWeight.util.WorldList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerWeight {

    public static int defaultMaxCapacity;
    private static boolean disableMovement;
    private static float minSpeed;
    private static float maxSpeed;
    private static double beginSlowdown;
    private static boolean blindPlayer;
    private static boolean preventJumpWhenOverWeight;

    private double weight;
    private double increasedCapacity;
    private UUID playerId;
    private int maxCapacity;
    private boolean isPlayerFrozen;
    private boolean isPlayerOverLimit;
    private boolean isBlind;
    private boolean isJumpPrevented;

    public PlayerWeight(double weight, UUID id) {
        this.weight = weight;
        this.playerId = id;
        maxCapacity = defaultMaxCapacity;
        increasedCapacity = 0.0;
        isPlayerFrozen = false;
        isPlayerOverLimit = false;
        isBlind = false;
        isJumpPrevented = false;
        changeSpeed();
    }

    public static void initialize(boolean disableMovement, int capacity, float min, float max, double bSlowdown,
            boolean blindAtMax, boolean preventJump) {
        PlayerWeight.disableMovement = disableMovement;
        defaultMaxCapacity = capacity;
        minSpeed = min;
        maxSpeed = max;
        beginSlowdown = bSlowdown;
        blindPlayer = blindAtMax;
        preventJumpWhenOverWeight = preventJump;
    }

    public boolean getIsPlayerOverLimit() {
        return isPlayerOverLimit;
    }

    public void setIsPlayerOverLimit(boolean isPlayerOverLimit) {
        this.isPlayerOverLimit = isPlayerOverLimit;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double newWeight) {
        weight = newWeight;
    }

    public void setIncreasedCapacity(double capacity) {
        increasedCapacity = capacity;
    }

    public double getMaxWeight() {
        return maxCapacity + increasedCapacity;
    }

    public void setMaxWeight(int max) {
        maxCapacity = max;
    }

    public void changeSpeed() {
        Player player = Bukkit.getPlayer(playerId);
        if (isPluginDisabledForUserOrWorld(player)) {
            player.setWalkSpeed(0.20f);
            allowJumpIfBlocked();
            return;
        }

        if (isPlayerOverLimit) {
            handleMaxCapacity(player);
            return;
        }

        if (weight > this.getMaxWeight()) {
            handleMaxCapacity(player);
            return;
        } else {
            if (isPlayerFrozen) {
                if (disableMovement) {
                    FreezePlayerEvent.unfreezePlayer(playerId);
                }
                isPlayerFrozen = false;
            }
            allowJumpIfBlocked();
        }

        if (blindPlayer && isBlind && player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            isBlind = false;
        }

        double speedAdjustment = 0.0;
        if (beginSlowdown > 0.0) {
            speedAdjustment = beginSlowdown * this.getMaxWeight();
        }
        float weightRatio = ((float) weight - (float) speedAdjustment)
                / ((float) this.getMaxWeight() - (float) speedAdjustment);

        float weightFloat = maxSpeed - (weightRatio * (maxSpeed - minSpeed));

        if (weight <= 0 || speedAdjustment >= weight) {
            weightFloat = maxSpeed;
        }

        player.setWalkSpeed(weightFloat);
    }

    private boolean isPluginDisabledForUserOrWorld(Player player) {
        WorldList worldList = WorldList.getInstance();
        if (player == null) {
            return true;
        } else if (player.hasPermission("inventoryweight.off")) {
            return true;
        } else if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return true;
        } else
            return !(worldList.isInventoryWeightEnabled(player.getWorld().getName()));
    }

    private void handleMaxCapacity(Player player) {
        String overlimitMsg = LanguageConfig.getConfig().getMessages().getOverLimitMessage();
        if (preventJumpWhenOverWeight) {
            if (!isJumpPrevented) {
                PreventJumpEvent.preventJump(playerId);
                isJumpPrevented = true;
            }
        } else {
            allowJumpIfBlocked();
        }
        if (isPlayerOverLimit && !isPlayerFrozen && overlimitMsg != null && !overlimitMsg.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    overlimitMsg));
            player.setWalkSpeed(minSpeed);
        }

        if (!isPlayerFrozen && !disableMovement && !isPlayerOverLimit) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getOverWeightMessage()));
        }

        if (disableMovement && !isPlayerFrozen) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LanguageConfig.getConfig().getMessages().getCantMoveMessage()));
            FreezePlayerEvent.freezePlayer(playerId);
            isPlayerFrozen = true;
        }
        player.setWalkSpeed(minSpeed);
        isPlayerFrozen = true;

        if (blindPlayer) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1);
            player.addPotionEffect(blindness);
            isBlind = true;
        }
    }

    private void allowJumpIfBlocked() {
        if (isJumpPrevented) {
            PreventJumpEvent.allowJump(playerId);
            isJumpPrevented = false;
        }
    }

    public String getSpeedDisplay() {
        StringBuilder speedDisplay = new StringBuilder();
        double ratio = weight / this.getMaxWeight();
        int result = 20 - (int) (ratio * 20.0);
        for (int i = 0; i < 20; i++) {
            if (i < result) {
                speedDisplay.append(ChatColor.GREEN);
                speedDisplay.append("|");
            } else {
                speedDisplay.append(ChatColor.RED);
                speedDisplay.append("|");
            }
        }
        return speedDisplay.toString();
    }

    public int getPercentage() {
        double speedAdjustment = 0.0;
        if (beginSlowdown > 0.0) {
            speedAdjustment = beginSlowdown * this.getMaxWeight();
        }
        double ratio = (weight - speedAdjustment) / (this.getMaxWeight() - speedAdjustment);
        if (ratio <= 0) {
            return 100;
        }
        int finalRatio = (100 - (int) (ratio * 100.0));
        if (finalRatio < 0) {
            return 0;
        } else {
            return finalRatio;
        }
    }
}
