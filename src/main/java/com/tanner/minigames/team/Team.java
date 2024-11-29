package com.tanner.minigames.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Team {
    RED(ChatColor.RED + "Red", Material.RED_WOOL),
    BLUE(ChatColor.BLUE + "Blue", Material.BLUE_WOOL),
    GREEN(ChatColor.GREEN + "Green", Material.GREEN_WOOL),
    YELLOW(ChatColor.YELLOW + "Yellow", Material.YELLOW_WOOL),
    AQUA(ChatColor.AQUA + "Aqua", Material.LIGHT_BLUE_WOOL),
    PURPLE(ChatColor.DARK_PURPLE + "Purple", Material.PURPLE_WOOL),
    BLACK(ChatColor.BLACK + "Black", Material.BLACK_WOOL),
    WHITE(ChatColor.WHITE + "White", Material.WHITE_WOOL);


    private String display;
    private Material material;

    Team(String display, Material material) {
        this.display = display;
        this.material = material;
    }

    public String getDisplay() { return display; }

    public Material getMaterial() { return material; }
}
