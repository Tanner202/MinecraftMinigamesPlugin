package com.tanner.minigames.kit;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.type.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.UUID;
import java.util.function.BiFunction;

public enum TNTWarsKitType implements KitType {
    LAST_CHANCE(ChatColor.GREEN + "Last Chance", Material.EMERALD, "An upwards dash when in a sticky situation", LastChanceKit::new),
    BUILDER(ChatColor.BLUE + "Builder", Material.OAK_PLANKS, "Ability to place a limited amount of blocks", BuilderKit::new),
    SNEAKY(ChatColor.DARK_PURPLE + "Sneaky", Material.INK_SAC, "Ability to go invisible for a short duration", SneakyKit::new),
    DEFLECTOR(ChatColor.DARK_GREEN + "Deflector", Material.SHIELD, "Ability to hit tnt back at opponents", DeflectorKit::new),
    BLINDER(ChatColor.DARK_GRAY + "Blinder", Material.ENDER_EYE, "Ability to blind opponents", BlinderKit::new),
    FREEZER(ChatColor.AQUA + "Freezer", Material.ICE, "Ability to freeze opponents in place", FreezerKit::new);

    private String display, description;
    private Material material;
    private BiFunction<Minigames, UUID, Kit> kitFactory;

    TNTWarsKitType(String display, Material material, String description, BiFunction<Minigames, UUID, Kit> kitFactory) {
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

    public TNTWarsKitType[] getKits() { return values(); }
}
