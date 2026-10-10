package com.tanner.minigames.instance;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum GameType {
    COLORSWAP(ChatColor.BLUE + "Color Swap", Material.WHITE_WOOL),
    DRAGON_ESCAPE(ChatColor.DARK_PURPLE + "Dragon Escape", Material.ENDER_DRAGON_SPAWN_EGG),
    SPLEEF(ChatColor.AQUA + "Spleef", Material.DIAMOND_SHOVEL),
    TNT_WARS(ChatColor.RED + "TNT Wars", Material.TNT);

    private String displayName;
    private Material displayIcon;

    GameType(String displayName, Material displayIcon) {
        this.displayName = displayName;
        this.displayIcon = displayIcon;
    }

    public String getDisplayName() { return displayName; }
    public Material getDisplayIcon() { return displayIcon; }
}
