package com.tanner.minigames.manager;

import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaManager {

    private List<Arena> arenas = new ArrayList<>();

    private FileConfiguration config;

    public ArenaManager(Minigames minigames) {
        config = minigames.getConfig();

        addArenasFromConfig(minigames);
    }

    private void addArenasFromConfig(Minigames minigames) {
        for (String arenaID : config.getConfigurationSection("arenas").getKeys(false)) {
            if (!isValidArena(arenaID)) {
                minigames.getLogger().warning("Arena ID: " + arenaID + " has missing or invalid values in the config file.");
                continue;
            }
            String gameName = config.getString("arenas." + arenaID + ".game");
            GameType gameType;
            try {
                gameType = GameType.valueOf(gameName);
            } catch (IllegalArgumentException e) {
                minigames.getLogger().warning("Could not find gamemode of type " + gameName + " which is used in the config");
                continue;
            }
            arenas.add(new Arena(minigames,
                    Integer.parseInt(arenaID),
                    getArenaLocation(arenaID),
                    gameType,
                    getNPCSpawn(arenaID),
                    config.getInt("arenas." + arenaID + ".amount-of-teams"),
                    config.getInt("arenas." + arenaID + ".max-players"),
                    config.getBoolean("arenas." + arenaID + ".world-reload-enabled")));
        }
    }

    private boolean isValidArena(String arenaID) {
        String path = "arenas." + arenaID;
        if (!config.contains(path + ".game") ||
                !config.contains(path + ".amount-of-teams") ||
                !config.contains(path + ".max-players") ||
                !config.contains(path + ".world-reload-enabled")) {
            return false;
        }

        if (!ConfigManager.isValidLocation("arenas." + arenaID)) {
            return false;
        }

        try {
            Integer.parseInt(arenaID);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
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

    private Location getNPCSpawn(String arenaID) {
        String npcSpawnPrefix = "arenas." + arenaID + ".npc-spawn";
        if (config.getString(npcSpawnPrefix + ".world") == null) return null;

        return new Location(
                Bukkit.getWorld(config.getString(npcSpawnPrefix + ".world")),
                config.getDouble(npcSpawnPrefix + ".x"),
                config.getDouble(npcSpawnPrefix + ".y"),
                config.getDouble(npcSpawnPrefix + ".z"),
                (float) config.getDouble(npcSpawnPrefix + ".yaw"),
                (float) config.getDouble(npcSpawnPrefix + ".pitch"));
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

    public Arena getArena(World world) {
        for (Arena arena : arenas) {
            if (arena.getWorld().getName().equals(world.getName())) {
                return arena;
            }
        }
        return null;
    }

    public int getArena(UUID npcUUID) {
        for (Arena arena : arenas) {
            if (arena.getNPC() != null && arena.getNPC().getUniqueId().equals(npcUUID)) {
                return arena.getId();
            }
        }
        return -1;
    }
}
