package com.tanner.minigames.manager;

import com.tanner.minigames.util.GameSettings;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaManager {

    private Minigames minigames;
    private List<Arena> arenas = new ArrayList<>();

    private FileConfiguration config;

    public ArenaManager(Minigames minigames) {
        this.minigames = minigames;
        config = minigames.getConfig();

        addArenasFromConfig();
    }

    private void addArenasFromConfig() {
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

            GameSettings gameSettings = new GameSettings(
                    gameType,
                    getArenaLocation(arenaID),
                    getNPCSpawn(arenaID),
                    config.getInt("arenas." + arenaID + ".team-size"),
                    config.getInt("arenas." + arenaID + ".max-players"),
                    config.getBoolean("arenas." + arenaID + ".world-reload-enabled"));

            arenas.add(new Arena(minigames,
                    Integer.parseInt(arenaID),
                    gameSettings));
        }
    }

    private boolean isValidArena(String arenaID) {
        String path = "arenas." + arenaID;
        if (!config.contains(path + ".game") ||
                !config.contains(path + ".team-size") ||
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

    public Arena addArena(GameSettings gameSettings) {
        int newID = arenas.getLast().getId() + 1;
        Arena arena = new Arena(minigames,
                newID,
                gameSettings);
        arenas.add(arena);
        return arena;
    }

    public void deleteArena(Arena arena) {
        if (arena.getNPC() != null) {
            arena.getNPC().remove();
            arena.getNPCHologram().removeHologram();
        }
        arenas.remove(arena);
        config.set("arenas." + arena.getId(), null);
        minigames.saveConfig();
    }

    public void saveArena(Arena arena) {
        int id = arena.getId();

        ConfigurationSection arenaSection = config.createSection("arenas." + id);
        arenaSection.set(".game", arena.getGameType().toString());
        arenaSection.set(".max-players", arena.getPlayerLimit());
        arenaSection.set(".team-size", arena.getTeamSize());
        arenaSection.set(".world-reload-enabled", arena.worldReloadEnabled());

        Location spawn = arena.getSpawn();
        arenaSection.set(".world", spawn.getWorld().getName());
        arenaSection.set(".x", spawn.getX());
        arenaSection.set(".y", spawn.getY());
        arenaSection.set(".z", spawn.getZ());
        arenaSection.set(".yaw", spawn.getYaw());
        arenaSection.set(".pitch", spawn.getPitch());

        ConfigurationSection npcSpawnConfigSection = arenaSection.createSection("npc-spawn");
        if (arena.getNPC() != null) {
            Location npcSpawn = arena.getNPC().getLocation();
            npcSpawnConfigSection.set(".world", npcSpawn.getWorld().getName());
            npcSpawnConfigSection.set(".x", npcSpawn.getX());
            npcSpawnConfigSection.set(".y", npcSpawn.getY());
            npcSpawnConfigSection.set(".z", npcSpawn.getZ());
            npcSpawnConfigSection.set(".yaw", npcSpawn.getYaw());
            npcSpawnConfigSection.set(".pitch", npcSpawn.getPitch());
        }
        minigames.saveConfig();
    }
}
