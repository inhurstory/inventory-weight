package me.wonka01.InventoryWeight.playerweight;

import java.util.UUID;

public class PlayerLevelState {

    private final UUID playerId;
    private int level;
    private double baseWeightLimit;
    private double multiplierPerLevel;
    private double cachedEffectiveWeightLimit;

    public PlayerLevelState(UUID playerId, int level, double baseWeightLimit, double multiplierPerLevel) {
        this.playerId = playerId;
        this.level = level;
        this.baseWeightLimit = baseWeightLimit;
        this.multiplierPerLevel = multiplierPerLevel;
        recalculate();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getLevel() {
        return level;
    }

    public double getEffectiveWeightLimit() {
        return cachedEffectiveWeightLimit;
    }

    public double getBaseWeightLimit() {
        return baseWeightLimit;
    }

    public double getMultiplierPerLevel() {
        return multiplierPerLevel;
    }

    public void update(int newLevel, double newBaseWeightLimit, double newMultiplier) {
        this.level = newLevel;
        this.baseWeightLimit = newBaseWeightLimit;
        this.multiplierPerLevel = newMultiplier;
        recalculate();
    }

    public void updateBaseWeightLimit(double newBaseWeightLimit) {
        this.baseWeightLimit = newBaseWeightLimit;
        recalculate();
    }

    public void updateMultiplier(double newMultiplier) {
        this.multiplierPerLevel = newMultiplier;
        recalculate();
    }

    private void recalculate() {
        cachedEffectiveWeightLimit = baseWeightLimit * Math.pow(multiplierPerLevel, level - 1);
    }
}
