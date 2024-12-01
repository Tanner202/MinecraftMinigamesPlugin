package com.tanner.minigames.kit;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.kittype.colorswap.FisherKit;
import com.tanner.minigames.kit.kittype.colorswap.SnowballerKit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.UUID;
import java.util.function.BiFunction;

public enum ColorSwapKitType implements KitType {
    FISHER(ChatColor.BLUE + "Fisher", Material.COD, "Fish your enemies into the void.", FisherKit::new),
    BALLER(ChatColor.DARK_PURPLE + "Baller", Material.SNOWBALL, "Snowball your enemies into the void", SnowballerKit::new);

    private String display, description;
    private Material material;
    private BiFunction<Minigames, UUID, Kit> kitFactory;

    ColorSwapKitType(String display, Material material, String description, BiFunction<Minigames, UUID, Kit> kitFactory) {
        this.display = display;
        this.material = material;
        this.description = description;
        this.kitFactory = kitFactory;
    }

    public String getDisplay() { return display; }
    public Material getMaterial() { return material; }
    public String getDescription() { return description; }
    public String getName() { return name(); }

    public Kit createKit(Minigames minigames, UUID uuid) {
        return kitFactory.apply(minigames, uuid);
    }
}
