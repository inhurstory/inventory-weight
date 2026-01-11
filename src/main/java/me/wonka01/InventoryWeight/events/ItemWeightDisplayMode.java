package me.wonka01.InventoryWeight.events;

public enum ItemWeightDisplayMode {
    ACTIONBAR,
    CHAT,
    TITLE,
    SUBTITLE,
    SCOREBOARD,
    BOSSBAR,
    OFF;

    public static ItemWeightDisplayMode fromConfig(String raw) {
        if (raw == null) {
            return ACTIONBAR;
        }
        switch (raw.toLowerCase()) {
        case "chat":
            return CHAT;
        case "title":
            return TITLE;
        case "subtitle":
            return SUBTITLE;
        case "scoreboard":
            return SCOREBOARD;
        case "bossbar":
            return BOSSBAR;
        case "off":
            return OFF;
        default:
            return ACTIONBAR;
        }
    }
}
