package me.wonka01.InventoryWeight.events;

import me.wonka01.InventoryWeight.InventoryWeight;
import me.wonka01.InventoryWeight.configuration.LanguageConfig;
import me.wonka01.InventoryWeight.util.InventoryCheckUtil;
import me.wonka01.InventoryWeight.util.WorldList;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemWeightPreviewListener implements Listener {

    private final InventoryWeight plugin;
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
    private final Map<UUID, Scoreboard> previousScoreboards = new HashMap<UUID, Scoreboard>();
    private final Map<UUID, Integer> scoreboardTasks = new HashMap<UUID, Integer>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<UUID, BossBar>();

    public ItemWeightPreviewListener(InventoryWeight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        if (!plugin.isItemWeightPreviewEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!WorldList.getInstance().isInventoryWeightEnabled(player)) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("inventoryweight.off")) {
            return;
        }

        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        double weight = InventoryCheckUtil.getItemWeight(item);

        sendPreview(player, weight);
    }

    private void sendPreview(Player player, double weight) {
        ItemWeightDisplayMode mode = plugin.getItemWeightPreviewMode();
        if (mode == ItemWeightDisplayMode.OFF) {
            return;
        }

        switch (mode) {
        case CHAT:
            sendChat(player, weight);
            break;
        case TITLE:
            sendTitle(player, weight, true);
            break;
        case SUBTITLE:
            sendTitle(player, weight, false);
            break;
        case SCOREBOARD:
            sendScoreboard(player, weight);
            break;
        case BOSSBAR:
            sendBossBar(player, weight);
            break;
        case ACTIONBAR:
        default:
            sendActionBar(player, weight);
            break;
        }
    }

    private void sendActionBar(Player player, double weight) {
        String template = LanguageConfig.getConfig().getMessages().getActionBarItemWeight();
        if (template == null || template.isEmpty()) {
            template = "&eItem weight: &a%weight%";
        }
        String message = template.replace("%weight%", decimalFormatter.format(weight));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void sendChat(Player player, double weight) {
        String template = LanguageConfig.getConfig().getMessages().getChatItemWeight();
        if (template == null || template.isEmpty()) {
            template = "&eItem weight: &a%weight%";
        }
        String message = template.replace("%weight%", decimalFormatter.format(weight));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void sendTitle(Player player, double weight, boolean showInTitle) {
        String titleTemplate = showInTitle ? LanguageConfig.getConfig().getMessages().getTitleItemWeightTitle()
                : LanguageConfig.getConfig().getMessages().getSubtitleItemWeightTitle();
        String subtitleTemplate = showInTitle ? LanguageConfig.getConfig().getMessages().getTitleItemWeightSubtitle()
                : LanguageConfig.getConfig().getMessages().getSubtitleItemWeightSubtitle();

        if (titleTemplate == null || titleTemplate.isEmpty()) {
            titleTemplate = showInTitle ? "&eItem weight" : "";
        }
        if (subtitleTemplate == null || subtitleTemplate.isEmpty()) {
            subtitleTemplate = "&a%weight%";
        }

        String title = ChatColor.translateAlternateColorCodes('&',
                titleTemplate.replace("%weight%", decimalFormatter.format(weight)));
        String subtitle = ChatColor.translateAlternateColorCodes('&',
                subtitleTemplate.replace("%weight%", decimalFormatter.format(weight)));
        player.sendTitle(title, subtitle, plugin.getTitleFadeIn(), plugin.getTitleStay(), plugin.getTitleFadeOut());
    }

    private void sendBossBar(Player player, double weight) {
        clearBossBar(player);
        String template = LanguageConfig.getConfig().getMessages().getBossBarItemWeight();
        if (template == null || template.isEmpty()) {
            template = "&eItem weight: &a%weight%";
        }
        String message = ChatColor.translateAlternateColorCodes('&',
                template.replace("%weight%", decimalFormatter.format(weight)));

        BarColor color = plugin.getBossBarColor();
        BarStyle style = plugin.getBossBarStyle();
        BossBar bossBar = Bukkit.createBossBar(message, color, style);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);
        activeBossBars.put(player.getUniqueId(), bossBar);

        new BukkitRunnable() {
            @Override
            public void run() {
                clearBossBar(player);
            }
        }.runTaskLater(plugin, plugin.getBossBarDurationTicks());
    }

    private void clearBossBar(Player player) {
        BossBar existing = activeBossBars.remove(player.getUniqueId());
        if (existing != null) {
            existing.removeAll();
        }
    }

    private void sendScoreboard(Player player, double weight) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }

        if (scoreboardTasks.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(scoreboardTasks.get(player.getUniqueId()));
            scoreboardTasks.remove(player.getUniqueId());
        }

        Scoreboard original = player.getScoreboard();
        previousScoreboards.put(player.getUniqueId(), original);

        Scoreboard board = manager.getNewScoreboard();
        String titleTemplate = LanguageConfig.getConfig().getMessages().getScoreboardItemWeightTitle();
        if (titleTemplate == null || titleTemplate.isEmpty()) {
            titleTemplate = "&eItem Weight";
        }
        String lineTemplate = LanguageConfig.getConfig().getMessages().getScoreboardItemWeightLine();
        if (lineTemplate == null || lineTemplate.isEmpty()) {
            lineTemplate = "&a%weight%";
        }

        String title = ChatColor.translateAlternateColorCodes('&',
                titleTemplate.replace("%weight%", decimalFormatter.format(weight)));
        String line = ChatColor.translateAlternateColorCodes('&',
                lineTemplate.replace("%weight%", decimalFormatter.format(weight)));

        Objective objective = board.registerNewObjective("iw_hotbar", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore(line).setScore(1);

        player.setScoreboard(board);

        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreScoreboard(player);
        }, plugin.getScoreboardDurationTicks()).getTaskId();
        scoreboardTasks.put(player.getUniqueId(), taskId);
    }

    private void restoreScoreboard(Player player) {
        if (scoreboardTasks.containsKey(player.getUniqueId())) {
            scoreboardTasks.remove(player.getUniqueId());
        }
        Scoreboard previous = previousScoreboards.remove(player.getUniqueId());
        if (previous != null) {
            player.setScoreboard(previous);
        }
    }
}
