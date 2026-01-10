package me.wonka01.InventoryWeight.playerweight;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class LevelScalingStrategy {

    public enum Mode {
        LINEAR,
        EXPONENTIAL
    }

    private final Mode mode;
    private final double capMultiplier;
    private final double linearMultiplierPerLevel;
    private final double exponentialSpan;
    private final double exponentialTau;
    private final int exponentialOffset;

    private LevelScalingStrategy(Mode mode,
            double capMultiplier,
            double linearMultiplierPerLevel,
            double exponentialSpan,
            double exponentialTau,
            int exponentialOffset) {
        this.mode = mode;
        this.capMultiplier = capMultiplier;
        this.linearMultiplierPerLevel = linearMultiplierPerLevel;
        this.exponentialSpan = exponentialSpan;
        this.exponentialTau = exponentialTau <= 0.0 ? 1.0 : exponentialTau;
        this.exponentialOffset = exponentialOffset;
    }

    public static LevelScalingStrategy fromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("leveling");
        if (section == null) {
            return defaultLinear(1.1, 0.0);
        }
        String modeValue = section.getString("mode", "linear").toLowerCase();
        Mode parsedMode = parseMode(modeValue);
        double cap = section.getDouble("capMultiplier", 0.0);

        double linearMultiplier = section.getDouble("multiplierPerLevel", 1.1);
        ConfigurationSection linearSection = section.getConfigurationSection("linear");
        if (linearSection != null) {
            linearMultiplier = linearSection.getDouble("multiplierPerLevel", linearMultiplier);
        }

        double expSpan = 2.5;
        double expTau = 10.0;
        int expOffset = 0;
        ConfigurationSection expSection = section.getConfigurationSection("exponential");
        if (expSection != null) {
            expSpan = expSection.getDouble("span", expSpan);
            expTau = expSection.getDouble("tau", expTau);
            expOffset = expSection.getInt("offset", expOffset);
        }

        return new LevelScalingStrategy(parsedMode, cap, linearMultiplier, expSpan, expTau, expOffset);
    }

    private static Mode parseMode(String value) {
        if ("exponential".equalsIgnoreCase(value)) {
            return Mode.EXPONENTIAL;
        }
        return Mode.LINEAR;
    }

    private static LevelScalingStrategy defaultLinear(double multiplierPerLevel, double capMultiplier) {
        return new LevelScalingStrategy(Mode.LINEAR, capMultiplier, multiplierPerLevel, 0.0, 1.0, 0);
    }

    public Mode getMode() {
        return mode;
    }

    public double computeEffectiveWeight(double baseWeightLimit, int level) {
        return baseWeightLimit * computeMultiplier(level);
    }

    public double computeMultiplier(int level) {
        int clampedLevel = Math.max(1, level);
        double multiplier;
        if (mode == Mode.EXPONENTIAL) {
            multiplier = computeExponential(clampedLevel);
        } else {
            multiplier = computeLinear(clampedLevel);
        }
        return applyCap(multiplier);
    }

    private double applyCap(double multiplier) {
        if (capMultiplier > 0.0) {
            return Math.min(multiplier, capMultiplier);
        }
        return multiplier;
    }

    private double computeLinear(int level) {
        if (level <= 1) {
            return 1.0;
        }
        return Math.pow(linearMultiplierPerLevel, level - 1);
    }

    private double computeExponential(int level) {
        int adjustedLevel = Math.max(0, level - 1 - exponentialOffset);
        double growth = 1.0 + exponentialSpan * (1 - Math.exp(-(adjustedLevel) / exponentialTau));
        if (growth < 1.0) {
            return 1.0;
        }
        return growth;
    }
}
