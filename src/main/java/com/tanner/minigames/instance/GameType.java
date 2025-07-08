package com.tanner.minigames.instance;

import org.bukkit.ChatColor;

public enum GameType {
    COLORSWAP(ChatColor.BLUE + "Color Swap"),
    DRAGON_ESCAPE(ChatColor.DARK_PURPLE + "Dragon Escape"),
    SCRAPYARD_SKIRMISH(ChatColor.YELLOW + "Scrapyard Skirmish"),
    SPLEEF(ChatColor.AQUA + "Spleef"),
    TNT_WARS(ChatColor.RED + "TNT Wars");

    private String displayName;

    GameType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
