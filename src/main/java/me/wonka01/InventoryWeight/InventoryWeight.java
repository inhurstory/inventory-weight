package me.wonka01.InventoryWeight;

import me.wonka01.InventoryWeight.commands.InventoryWeightCommands;
import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.events.FreezePlayerEvent;
import me.wonka01.InventoryWeight.events.JoinEvent;
import me.wonka01.InventoryWeight.events.ItemWeightPreviewListener;
import me.wonka01.InventoryWeight.events.PreventJumpEvent;
import me.wonka01.InventoryWeight.events.ItemWeightDisplayMode;
import me.wonka01.InventoryWeight.playerweight.LevelScalingStrategy;
import me.wonka01.InventoryWeight.playerweight.ItemLimit;
import me.wonka01.InventoryWeight.playerweight.PlayerLevelManager;
import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import me.wonka01.InventoryWeight.util.InventoryCheckUtil;
import me.wonka01.InventoryWeight.util.InventoryWeightExpansion;
import me.wonka01.InventoryWeight.util.WorldList;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class InventoryWeight extends JavaPlugin {

    private InventoryWeightCommands commands;
    private LanguageConfig languageConfig;
    private PlayerLevelManager levelManager;
    private boolean itemWeightPreviewEnabled;
    private ItemWeightDisplayMode itemWeightPreviewMode;
    private static final int TITLE_FADE_IN = 5;
    private static final int TITLE_STAY = 40;
    private static final int TITLE_FADE_OUT = 10;
    private static final int SCOREBOARD_DURATION_TICKS = 100;
    private static final int BOSSBAR_DURATION_TICKS = 60;
    private static final BarColor BOSSBAR_COLOR = BarColor.GREEN;
    private static final BarStyle BOSSBAR_STYLE = BarStyle.SOLID;

    @Override
    public void onEnable() {
        getLogger().info("onEnable is called!");
        registerEvents();

        commands = new InventoryWeightCommands();
        commands.setup();
        PluginCommand pluginCommand = getCommand("inventoryweight");
        pluginCommand.setExecutor(commands);
        pluginCommand.setTabCompleter(commands);

        saveDefaultConfig();
        initConfig();
        setUpMessageConfig();
        setUpLevelingConfig();

        BukkitScheduler scheduler = getServer().getScheduler();
        int timer = getConfig().getInt("checkInventoryTime");
        if (timer < 1) {
            timer = 3;
        }

        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                HashMap<UUID, PlayerWeight> mapReference = PlayerWeightMap.getPlayerWeightMap();
                Iterator<Entry<UUID, PlayerWeight>> hmIterator = mapReference.entrySet().iterator();

                while (hmIterator.hasNext()) {
                    Entry<UUID, PlayerWeight> element = hmIterator.next();
                    UUID playerId = element.getKey();
                    PlayerWeight playerWeight = element.getValue();
                    Server server = getServer();
                    if (server.getPlayer(playerId) != null) {
                        Player player = server.getPlayer(playerId);
                        double baseWeightLimit = getBaseWeightLimit(player);
                        double effectiveMaxWeight = baseWeightLimit;
                        int level = 1;
                        if (levelManager != null && levelManager.isEnabled()) {
                            effectiveMaxWeight = levelManager.getEffectiveMaxWeight(playerId, baseWeightLimit);
                            level = levelManager.getLevel(playerId);
                        }
                        double effectiveMultiplier = getEffectiveMultiplier(playerId, baseWeightLimit);
                        playerWeight.applyLevelData(baseWeightLimit, level, effectiveMultiplier, effectiveMaxWeight);
                        Inventory inv = player.getInventory();

                        if (inv != null) {
                            Map<String, Double> weightMap = InventoryCheckUtil.getInventoryWeight(inv.getContents(),
                                    player);
                            boolean isOverLimit = weightMap.get("overLimit") > 0.0;
                            playerWeight.setIsPlayerOverLimit(isOverLimit);
                            playerWeight.setWeight(weightMap.get("totalWeight"));
                            playerWeight.setIncreasedCapacity(weightMap.get("totalIncreasedCapacity"));
                            playerWeight.changeSpeed();
                        }
                    }
                }
            }
        }, 0L, (timer * 20));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new InventoryWeightExpansion().register();
        }

        // add all players to map
        for (Player player : this.getServer().getOnlinePlayers()) {
            JoinEvent.addPlayerToWeightMap(player);
        }
    }

    private int getMaxWeightPerm(Player player) {
        Set<PermissionAttachmentInfo> set = player.getEffectivePermissions();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            PermissionAttachmentInfo info = (PermissionAttachmentInfo) iterator.next();
            if (info.getPermission().contains("inventoryweight.maxweight.")) {
                String amount = info.getPermission().substring(26);
                return Integer.parseInt(amount);
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
        if (levelManager != null) {
            levelManager.shutdown();
        }
    }

    private void initConfig() {

        boolean disableMovement = getConfig().getBoolean("disableMovement");
        boolean blindPlayer = getConfig().getBoolean("blindAtMax");
        int capacity = getConfig().getInt("weightLimit");
        boolean armorOnly = getConfig().getBoolean("armorOnly");
        InventoryCheckUtil.armorOnlyMode = armorOnly;

        float minWeight = (float) getConfig().getDouble("minWalkSpeed");
        float maxWeight = (float) getConfig().getDouble("maxWalkSpeed");

        double beginSlowdown = getConfig().getDouble("beginSlowdown", 0.0);
        double beginPreventJump = getConfig().getDouble("beginPreventJump", 1.0);

        List<?> matWeights = getConfig().getList("materialWeights");
        List<?> nameWeights = getConfig().getList("customItemWeights");
        List<?> itemLimits = getConfig().getList("itemLimits");
        List<?> loreWeights = getConfig().getList("customLoreWeights");
        List<?> customModelWeights = getConfig().getList("modelIdWeights");

        InventoryCheckUtil.defaultWeight = getConfig().getDouble("defaultWeight");
        InventoryCheckUtil.mapOfItemLimits.clear();
        InventoryCheckUtil.mapOfWeightsByMaterial.clear();
        InventoryCheckUtil.mapOfWeightsByLore.clear();
        InventoryCheckUtil.mapOfWeightsByDisplayName.clear();

        if (matWeights != null) {
            for (Object item : matWeights) {
                LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                double weight = getDoubleFromConfigValue(map.get("weight"));
                String upperCaseMaterial = ((String) map.get("material")).toUpperCase();
                InventoryCheckUtil.mapOfWeightsByMaterial.put(upperCaseMaterial, weight);
            }
        }

        if (nameWeights != null) {
            for (Object item : nameWeights) {
                LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                double weight = getDoubleFromConfigValue(map.get("weight"));
                String itemName = (String) map.get("name");

                InventoryCheckUtil.mapOfWeightsByDisplayName.put(itemName, weight);
            }
        }

        if (itemLimits != null) {
            for (Object item : itemLimits) {
                LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                int limit = (Integer) map.get("limit");
                Integer modelId = (Integer) map.get("modelId");
                String itemName = (String) map.get("material");
                String permissionNode = (String) map.get("permission");

                ItemLimit itemLimit;
                if (modelId != null && modelId != -1) {
                    itemLimit = new ItemLimit(limit, permissionNode, modelId);
                } else {
                    itemLimit = new ItemLimit(limit, permissionNode, itemName);
                }

                InventoryCheckUtil.mapOfItemLimits.add(itemLimit);

            }
        }

        if (loreWeights != null) {
            for (Object item : loreWeights) {
                LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                double weight = getDoubleFromConfigValue(map.get("weight"));
                String itemName = (String) map.get("name");

                InventoryCheckUtil.mapOfWeightsByLore.put(itemName, weight);
            }
        }

        if (customModelWeights != null) {
            for (Object item : customModelWeights) {
                LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                double weight = getDoubleFromConfigValue(map.get("weight"));
                int modelId = (Integer) map.get("modelId");
                InventoryCheckUtil.mapOfWeightsByCustomModelId.put(modelId, weight);
            }
        }

        InventoryCheckUtil.loreTag = getConfig().getString("loreTag");
        InventoryCheckUtil.capacityTag = getConfig().getString("capacityTag");

        PlayerWeight.initialize(disableMovement, capacity, minWeight, maxWeight, beginSlowdown, blindPlayer,
                beginPreventJump);

        List<String> worlds = getConfig().getStringList("worlds");
        WorldList.initializeWorldList(worlds);

        loadItemWeightPreviewConfig();
    }

    private double getDoubleFromConfigValue(Object weight) {
        if (weight instanceof Double) {
            return (Double) weight;
        } else {
            int tempInt = (Integer) weight;
            return (double) tempInt;
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new FreezePlayerEvent(), this);
        getServer().getPluginManager().registerEvents(new PreventJumpEvent(), this);
        getServer().getPluginManager().registerEvents(new ItemWeightPreviewListener(this), this);
    }

    private void setUpMessageConfig() {
        languageConfig = new LanguageConfig();
        languageConfig.setUpLanguageConfig();
    }

    private void setUpLevelingConfig() {
        if (levelManager != null) {
            levelManager.shutdown();
        }
        boolean enabled = getConfig().getBoolean("leveling.enabled", true);
        int defaultLevel = getConfig().getInt("leveling.defaultLevel", 1);
        int maxLevel = getConfig().getInt("leveling.maxLevel", defaultLevel);
        LevelScalingStrategy strategy = LevelScalingStrategy.fromConfig(getConfig());
        levelManager = new PlayerLevelManager(this);
        levelManager.configure(enabled, defaultLevel, maxLevel, strategy);
    }

    private void loadItemWeightPreviewConfig() {
        itemWeightPreviewEnabled = getConfig().getBoolean("itemWeightPreview.enabled", true);
        String mode = getConfig().getString("itemWeightPreview.mode", "actionbar");
        itemWeightPreviewMode = ItemWeightDisplayMode.fromConfig(mode);
    }

    public void reloadConfiguration() {
        reloadConfig();
        setUpMessageConfig();
        initConfig();
        setUpLevelingConfig();
    }

    public PlayerLevelManager getLevelManager() {
        return levelManager;
    }

    public double getEffectiveMultiplier(UUID playerId, double baseWeightLimit) {
        if (levelManager == null || !levelManager.isEnabled()) {
            return 1.0;
        }
        return levelManager.getEffectiveMultiplier(playerId, baseWeightLimit);
    }

    public int getBaseWeightLimit(Player player) {
        int fromPerm = getMaxWeightPerm(player);
        if (fromPerm != -1) {
            return fromPerm;
        }
        return getConfig().getInt("weightLimit");
    }

    public boolean isItemWeightPreviewEnabled() {
        return itemWeightPreviewEnabled;
    }

    public ItemWeightDisplayMode getItemWeightPreviewMode() {
        return itemWeightPreviewMode;
    }

    public int getBossBarDurationTicks() {
        return BOSSBAR_DURATION_TICKS;
    }

    public BarColor getBossBarColor() {
        return BOSSBAR_COLOR;
    }

    public BarStyle getBossBarStyle() {
        return BOSSBAR_STYLE;
    }

    public int getTitleFadeIn() {
        return TITLE_FADE_IN;
    }

    public int getTitleStay() {
        return TITLE_STAY;
    }

    public int getTitleFadeOut() {
        return TITLE_FADE_OUT;
    }

    public int getScoreboardDurationTicks() {
        return SCOREBOARD_DURATION_TICKS;
    }
}
