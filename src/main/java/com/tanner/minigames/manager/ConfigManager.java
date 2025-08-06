package com.tanner.minigames.manager;

import com.tanner.minigames.util.Constants;
import com.tanner.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static FileConfiguration config;

    public static void setupConfig(Minigames minigames) {
        ConfigManager.config = minigames.getConfig();
        minigames.saveDefaultConfig();
    }

    public static int getRequiredPlayers() { return config.getInt("required-players", Constants.DEFAULT_REQUIRED_PLAYERS); }

    public static int getCountdownSeconds() { return config.getInt("countdown-seconds", Constants.DEFAULT_COUNTDOWN_TIME); }

    public static Location getSpawn(String path) {
        if (isValidLocation(path)) {
            return new Location(
                    Bukkit.getWorld(config.getString(path + ".world")),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z"),
                    (float) config.getDouble(path + ".yaw", 0.0),
                    (float) config.getDouble(path + ".pitch", 0.0));
        } else {
            Bukkit.getLogger().warning("[Minigames] Invalid or missing config location: " + path);
            return null;
        }
    }

    public static Location getLobbySpawn() {
        return getSpawn("lobby-spawn");
    }

    public static boolean isValidLocation(String path) {
        if (!config.contains(path + ".world") ||
                !config.contains(path + ".x") ||
                !config.contains(path + ".y") ||
                !config.contains(path + ".z")) {
            return false;
        }

        String worldName = config.getString(path + ".world");
        if (Bukkit.getWorld(worldName) == null) {
            return false;
        }

        return true;
    }

    public static void setLocation(String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

}
