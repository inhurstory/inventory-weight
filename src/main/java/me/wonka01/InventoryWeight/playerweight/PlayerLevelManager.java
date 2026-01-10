package me.wonka01.InventoryWeight.playerweight;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.wonka01.InventoryWeight.playerweight.LevelScalingStrategy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLevelManager {

    private final InventoryWeight plugin;
    private final Map<UUID, PlayerLevelState> levelCache = new HashMap<>();
    private File levelFile;
    private YamlConfiguration yamlConfiguration;
    private boolean enabled;
    private int defaultLevel;
    private int maxLevel;
    private LevelScalingStrategy scalingStrategy;

    public PlayerLevelManager(InventoryWeight plugin) {
        this.plugin = plugin;
    }

    public void configure(boolean enabled, int defaultLevel, int maxLevel, LevelScalingStrategy scalingStrategy) {
        this.enabled = enabled;
        this.defaultLevel = Math.max(1, defaultLevel);
        if (scalingStrategy == null) {
            this.scalingStrategy = LevelScalingStrategy.fromConfig(plugin.getConfig());
        } else {
            this.scalingStrategy = scalingStrategy;
        }
        this.maxLevel = Math.max(this.defaultLevel, maxLevel);
        if (!enabled) {
            levelCache.clear();
            return;
        }
        initFile();
        loadFromDisk();
    }

    public int getLevel(UUID playerId) {
        if (!enabled) {
            return defaultLevel;
        }
        return getOrCreateState(playerId, getDefaultWeightLimit()).getLevel();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LevelScalingStrategy getScalingStrategy() {
        return scalingStrategy;
    }

    public double getEffectiveMaxWeight(UUID playerId, double baseWeightLimit) {
        if (!enabled) {
            return baseWeightLimit;
        }
        if (scalingStrategy == null) {
            return baseWeightLimit;
        }
        PlayerLevelState state = getOrCreateState(playerId, baseWeightLimit);
        if (Math.abs(baseWeightLimit - state.getBaseWeightLimit()) > 0.0001) {
            state.updateBaseWeightLimit(baseWeightLimit);
        }
        return scalingStrategy.computeEffectiveWeight(baseWeightLimit, state.getLevel());
    }

    public double getEffectiveMultiplier(UUID playerId, double baseWeightLimit) {
        if (!enabled) {
            return 1.0;
        }
        if (scalingStrategy == null) {
            return 1.0;
        }
        PlayerLevelState state = getOrCreateState(playerId, baseWeightLimit);
        if (Math.abs(baseWeightLimit - state.getBaseWeightLimit()) > 0.0001) {
            state.updateBaseWeightLimit(baseWeightLimit);
        }
        double multiplier = scalingStrategy.computeMultiplier(state.getLevel());
        if (multiplier <= 0.0) {
            return 1.0;
        }
        return multiplier;
    }

    public void setLevel(UUID playerId, int newLevel, double baseWeightLimit) {
        if (!enabled) {
            return;
        }
        int clamped = clampLevel(newLevel);
        PlayerLevelState state = getOrCreateState(playerId, baseWeightLimit);
        state.update(clamped, baseWeightLimit);
        writeToDisk();
    }

    public void addLevel(UUID playerId, int delta, double baseWeightLimit) {
        if (!enabled) {
            return;
        }
        PlayerLevelState state = getOrCreateState(playerId, baseWeightLimit);
        int updated = clampLevel(state.getLevel() + delta);
        state.update(updated, baseWeightLimit);
        writeToDisk();
    }

    public void saveNow() {
        if (!enabled) {
            return;
        }
        writeToDisk();
    }

    public void shutdown() {
        saveNow();
    }

    private void initFile() {
        levelFile = new File(plugin.getDataFolder(), "player-levels.yml");
        if (!levelFile.exists()) {
            levelFile.getParentFile().mkdirs();
            try {
                levelFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yamlConfiguration = new YamlConfiguration();
        try {
            yamlConfiguration.load(levelFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromDisk() {
        if (!enabled) {
            return;
        }
        if (!yamlConfiguration.contains("players")) {
            return;
        }
        for (String key : yamlConfiguration.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                int level = yamlConfiguration.getInt("players." + key + ".level", defaultLevel);
                level = clampLevel(level);
                levelCache.put(id, new PlayerLevelState(id, level, getDefaultWeightLimit()));
            } catch (IllegalArgumentException e) {
                // ignore malformed UUID entries
            }
        }
    }

    private void writeToDisk() {
        if (!enabled || yamlConfiguration == null) {
            return;
        }
        for (Map.Entry<UUID, PlayerLevelState> entry : levelCache.entrySet()) {
            UUID id = entry.getKey();
            PlayerLevelState state = entry.getValue();
            yamlConfiguration.set("players." + id.toString() + ".level", state.getLevel());
        }
        try {
            yamlConfiguration.save(levelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PlayerLevelState getOrCreateState(UUID playerId, double baseWeightLimit) {
        if (!enabled) {
            return new PlayerLevelState(playerId, defaultLevel, baseWeightLimit);
        }
        if (!levelCache.containsKey(playerId)) {
            levelCache.put(playerId, new PlayerLevelState(playerId, defaultLevel, baseWeightLimit));
        }
        return levelCache.get(playerId);
    }

    private int clampLevel(int level) {
        if (level < 1) {
            return 1;
        }
        if (maxLevel > 0 && level > maxLevel) {
            return maxLevel;
        }
        return level;
    }

    private double getDefaultWeightLimit() {
        return plugin.getConfig().getInt("weightLimit");
    }
}
