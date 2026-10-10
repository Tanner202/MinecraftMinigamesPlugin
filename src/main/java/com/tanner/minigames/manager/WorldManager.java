package com.tanner.minigames.manager;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.util.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;

public class WorldManager {

    public static final String COLORSWAP_WORLD = "minigames_colorswap";

    // Creates the default void worlds, but only if an arena in the config uses them.
    // This way servers with their own arenas don't get extra worlds. Must run before ArenaManager,
    // because ArenaManager skips arenas whose world isn't loaded.
    public static void loadDefaultWorlds(Minigames minigames) {
        ConfigurationSection arenas = minigames.getConfig().getConfigurationSection("arenas");
        if (arenas == null) return;

        for (String arenaID : arenas.getKeys(false)) {
            if (COLORSWAP_WORLD.equals(arenas.getString(arenaID + ".world"))) {
                loadVoidWorld(COLORSWAP_WORLD);
            }
        }
    }

    // Loads the world if its folder already exists, otherwise generates a new empty one.
    // The generator must be passed every time the world is loaded, including after a server restart.
    private static void loadVoidWorld(String worldName) {
        if (Bukkit.getWorld(worldName) != null) return;

        Bukkit.createWorld(new WorldCreator(worldName).generator(new VoidChunkGenerator()));
    }
}
