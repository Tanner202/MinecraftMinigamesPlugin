package com.tanner.minigames.manager;

import com.tanner.minigames.Constants;
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

    public static Location getLobbySpawn() {
        if (isValidLocation("lobby-spawn")) {
            return new Location(
                    Bukkit.getWorld(config.getString("lobby-spawn.world")),
                    config.getDouble("lobby-spawn.x"),
                    config.getDouble("lobby-spawn.y"),
                    config.getDouble("lobby-spawn.z"),
                    (float) config.getDouble("lobby-spawn.yaw", 0.0),
                    (float) config.getDouble("lobby-spawn.pitch", 0.0));
        } else {
            Bukkit.getLogger().warning("[Minigames] Invalid or missing config location: lobby-spawn");
            return null;
        }
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

}
