package com.tanner.minigames.instance.game.colorswap;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum GridColor {

    WHITE(ChatColor.WHITE + "White", Material.WHITE_WOOL),
    GREEN(ChatColor.GREEN + "Green", Material.GREEN_WOOL),
    BLUE(ChatColor.BLUE + "Blue", Material.BLUE_WOOL),
    RED(ChatColor.RED + "Red", Material.RED_WOOL),
    AQUA(ChatColor.AQUA + "Aqua", Material.LIGHT_BLUE_WOOL),
    YELLOW(ChatColor.YELLOW + "Yellow", Material.YELLOW_WOOL);

    private String display;
    private Material material;

    GridColor(String display, Material material) {
        this.display = display;
        this.material = material;
    }

    public String getDisplay() {
        return display;
    }

    public Material getMaterial() {
        return material;
    }
}
