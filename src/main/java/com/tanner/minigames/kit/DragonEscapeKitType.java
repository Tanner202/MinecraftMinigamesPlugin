package com.tanner.minigames.kit;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.kittype.colorswap.DoubleJumperKit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.UUID;
import java.util.function.BiFunction;

public enum DragonEscapeKitType implements KitType {
    DOUBLE_JUMPER(ChatColor.LIGHT_PURPLE + "Double Jumper", Material.GOLDEN_BOOTS, "Double jump to get out of trouble.", DoubleJumperKit::new);

    private String display, description;
    private Material material;
    private BiFunction<Minigames, UUID, Kit> kitFactory;

    DragonEscapeKitType(String display, Material material, String description, BiFunction<Minigames, UUID, Kit> kitFactory) {
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
