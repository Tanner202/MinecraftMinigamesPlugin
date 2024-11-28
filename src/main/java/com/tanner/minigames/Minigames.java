package com.tanner.minigames;

import com.tanner.minigames.command.ArenaCommand;
import com.tanner.minigames.listener.ConnectListener;
import com.tanner.minigames.listener.GameListener;
import com.tanner.minigames.manager.ArenaManager;
import com.tanner.minigames.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Minigames extends JavaPlugin {

    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        ConfigManager.setupConfig(this);
        arenaManager = new ArenaManager(this);

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        getCommand("arena").setExecutor(new ArenaCommand(this));
    }

    public ArenaManager getArenaManager() { return arenaManager; }
}
