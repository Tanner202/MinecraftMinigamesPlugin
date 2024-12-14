package com.tanner.minigames;

import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.listener.ArenaListener;
import com.tanner.minigames.listener.ConnectListener;
import com.tanner.minigames.listener.GameLobbyListener;
import com.tanner.minigames.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Minigames extends JavaPlugin {

    private Arena arena;

    @Override
    public void onEnable() {
        Constants.initializeConstants(this);
        ConfigManager.setupConfig(this);
        arena = new Arena(this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameLobbyListener(this), this);
    }

    public Arena getArena() { return arena; }
}
