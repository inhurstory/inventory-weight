package me.wonka01.InventoryWeight.playerweight;

import java.util.UUID;

public class PlayerLevelState {

    private final UUID playerId;
    private int level;
    private double baseWeightLimit;

    public PlayerLevelState(UUID playerId, int level, double baseWeightLimit) {
        this.playerId = playerId;
        this.level = level;
        this.baseWeightLimit = baseWeightLimit;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getLevel() {
        return level;
    }

    public double getBaseWeightLimit() {
        return baseWeightLimit;
    }

    public void update(int newLevel, double newBaseWeightLimit) {
        this.level = newLevel;
        this.baseWeightLimit = newBaseWeightLimit;
    }

    public void updateBaseWeightLimit(double newBaseWeightLimit) {
        this.baseWeightLimit = newBaseWeightLimit;
    }
}
