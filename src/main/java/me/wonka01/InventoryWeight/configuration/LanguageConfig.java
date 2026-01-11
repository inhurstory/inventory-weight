package me.wonka01.InventoryWeight.configuration;

import me.wonka01.InventoryWeight.InventoryWeight;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageConfig {

    private static LanguageConfig config;
    private YamlConfiguration yamlConfiguration;
    private MessagesModel messages;

    public static LanguageConfig getConfig() {
        if (config == null) {
            config = new LanguageConfig();
        }
        return config;
    }

    private InventoryWeight plugin;
    private File configFile;

    public LanguageConfig() {
        plugin = InventoryWeight.getPlugin(InventoryWeight.class);
        configFile = new File(plugin.getDataFolder(), "messages.yml");

        configFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        yamlConfiguration = new YamlConfiguration();
        try {
            yamlConfiguration.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessagesModel getMessages() {
        return messages;
    }

    public void setUpLanguageConfig() {
        String noPermission = yamlConfiguration.getString("noPermission");
        String invalidCommand = yamlConfiguration.getString("invalidCommand");
        String invalidMaterial = yamlConfiguration.getString("invalidMaterial");
        String itemWeight = yamlConfiguration.getString("itemWeight");
        String weight = yamlConfiguration.getString("weight");
        String speed = yamlConfiguration.getString("speed");
        String reloadCommand = yamlConfiguration.getString("reloadCommand");
        String helpMessage = yamlConfiguration.getString("helpMessage");
        String cantMove = yamlConfiguration.getString("cantMoveMessage");
        String overlimit = yamlConfiguration.getString("overLimitMessage");
        String overWeight = yamlConfiguration.getString("overWeightMessage");
        String slowdownWarning = yamlConfiguration.getString("slowdownWarningMessage");
        String preventJumpWarning = yamlConfiguration.getString("preventJumpWarningMessage");
        String levelInfo = yamlConfiguration.getString("levelInfo");
        String levelSet = yamlConfiguration.getString("levelSet");
        String levelAdd = yamlConfiguration.getString("levelAdd");
        String levelingDisabledMessage = yamlConfiguration.getString("levelingDisabledMessage");
        String invalidPlayer = yamlConfiguration.getString("invalidPlayer");
        String actionBarItemWeight = yamlConfiguration.getString("actionBarItemWeight");
        String chatItemWeight = yamlConfiguration.getString("chatItemWeight");
        String titleItemWeightTitle = yamlConfiguration.getString("titleItemWeightTitle");
        String titleItemWeightSubtitle = yamlConfiguration.getString("titleItemWeightSubtitle");
        String subtitleItemWeightTitle = yamlConfiguration.getString("subtitleItemWeightTitle");
        String subtitleItemWeightSubtitle = yamlConfiguration.getString("subtitleItemWeightSubtitle");
        String scoreboardItemWeightTitle = yamlConfiguration.getString("scoreboardItemWeightTitle");
        String scoreboardItemWeightLine = yamlConfiguration.getString("scoreboardItemWeightLine");
        String bossBarItemWeight = yamlConfiguration.getString("bossBarItemWeight");
        if (cantMove == null || cantMove.isEmpty()) {
            cantMove = "&cYou can't carry your weight anymore, you're going to need to drop some items!";
        }
        if (overlimit == null) {
            overlimit = "&cYou're over the item limit!";
        }

        if (overWeight == null) {
            overWeight = "&cYou're over your weight limit!";
        }

        if (slowdownWarning == null) {
            slowdownWarning = "&eYou're starting to feel your pack weighing you down.";
        }

        if (preventJumpWarning == null) {
            preventJumpWarning = "&cYou are too heavy to jump!";
        }
        if (levelInfo == null || levelInfo.isEmpty()) {
            levelInfo = "&eLevel: %level% | Multiplier: %multiplier% | Max Weight: %max%";
        }
        if (levelSet == null || levelSet.isEmpty()) {
            levelSet = "&aSet %player%'s level to %level%.";
        }
        if (levelAdd == null || levelAdd.isEmpty()) {
            levelAdd = "&aAdjusted %player%'s level. New level: %level%.";
        }
        if (levelingDisabledMessage == null || levelingDisabledMessage.isEmpty()) {
            levelingDisabledMessage = "&cLeveling system is disabled.";
        }
        if (invalidPlayer == null || invalidPlayer.isEmpty()) {
            invalidPlayer = "&cThat player could not be found.";
        }

        messages = new MessagesModel(noPermission, invalidCommand, invalidMaterial, itemWeight,
                weight, speed, reloadCommand, helpMessage, cantMove, overlimit, overWeight, slowdownWarning,
                preventJumpWarning, levelInfo, levelSet, levelAdd, levelingDisabledMessage, invalidPlayer,
                actionBarItemWeight, chatItemWeight, titleItemWeightTitle, titleItemWeightSubtitle,
                subtitleItemWeightTitle, subtitleItemWeightSubtitle, scoreboardItemWeightTitle,
                scoreboardItemWeightLine, bossBarItemWeight);
        config = this;
    }
}
