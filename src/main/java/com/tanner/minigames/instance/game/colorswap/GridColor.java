package com.tanner.minigames.instance.game.colorswap;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum GridColor {

    WHITE(ChatColor.WHITE + "White", Material.WHITE_WOOL, "§f"),
    GREEN(ChatColor.GREEN + "Green", Material.GREEN_WOOL, "§2"),
    BLUE(ChatColor.BLUE + "Blue", Material.BLUE_WOOL, "§9"),
    RED(ChatColor.RED + "Red", Material.RED_WOOL, "§c"),
    AQUA(ChatColor.AQUA + "Aqua", Material.LIGHT_BLUE_WOOL, "§b"),
    YELLOW(ChatColor.YELLOW + "Yellow", Material.YELLOW_WOOL, "§e");

    private String display;
    private Material material;
    private String colorCode;

    GridColor(String display, Material material, String colorCode) {
        this.display = display;
        this.material = material;
        this.colorCode = colorCode;
    }

    public String getDisplay() {
        return display;
    }

    public Material getMaterial() {
        return material;
    }
    public String getColorCode() { return colorCode; }
}
