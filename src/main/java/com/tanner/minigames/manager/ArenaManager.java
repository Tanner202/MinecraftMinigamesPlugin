package com.tanner.minigames.manager;

import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    private List<Arena> arenas = new ArrayList<>();

    private FileConfiguration config;

    public ArenaManager(Minigames minigames) {
        config = minigames.getConfig();

        addArenasFromConfig(minigames);
    }

    private void addArenasFromConfig(Minigames minigames) {
        for (String arenaID : config.getConfigurationSection("arenas").getKeys(false)) {
            arenas.add(new Arena(minigames, Integer.parseInt(arenaID), getArenaLocation(arenaID),
                    config.getString("arenas." + arenaID + ".game"),
                    config.getInt("arenas." + arenaID + ".players-per-team"),
                    config.getInt("arenas." + arenaID + ".max-players")));
        }
    }

    private Location getArenaLocation(String arenaID) {
        return new Location(
                Bukkit.getWorld(config.getString("arenas." + arenaID + ".world")),
                config.getDouble("arenas." + arenaID + ".x"),
                config.getDouble("arenas." + arenaID + ".y"),
                config.getDouble("arenas." + arenaID + ".z"),
                (float) config.getDouble("arenas." + arenaID + ".yaw"),
                (float) config.getDouble("arenas." + arenaID + ".pitch"));
    }

    public List<Arena> getArenas() { return arenas; }

    public Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }
        return null;
    }

    public Arena getArena(int id) {
        for (Arena arena : arenas) {
            if (arena.getId() == id) {
                return arena;
            }
        }
        return null;
    }
}
