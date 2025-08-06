package com.tanner.minigames;

import com.tanner.minigames.util.Constants;
import com.tanner.minigames.command.ArenaCommand;
import com.tanner.minigames.gui.ArenaManageGUI;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.dragonescape.SetDragonWaypointsCommand;
import com.tanner.minigames.instance.game.scrapshuffle.SetCrateCommand;
import com.tanner.minigames.listener.ArenaListener;
import com.tanner.minigames.listener.ConnectListener;
import com.tanner.minigames.listener.GameLobbyListener;
import com.tanner.minigames.manager.ArenaManager;
import com.tanner.minigames.manager.ConfigManager;
import com.tanner.minigames.manager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Minigames extends JavaPlugin {

    private ArenaManager arenaManager;
    private FileManager fileManager;

    @Override
    public void onEnable() {
        Constants.initializeConstants(this);
        ConfigManager.setupConfig(this);

        fileManager = new FileManager(this);
        fileManager.addFile("scrapyard_skirmish/crates.yml");
        fileManager.addFile("scrapyard_skirmish/walls.yml");
        fileManager.addFile("scrapyard_skirmish/crate_locations.yml");
        fileManager.addFile("dragon_escape/dragon_locations.yml");

        arenaManager = new ArenaManager(this);
        SetDragonWaypointsCommand setDragonWaypointsCommand = new SetDragonWaypointsCommand(this);

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameLobbyListener(this), this);
        Bukkit.getPluginManager().registerEvents(setDragonWaypointsCommand, this);
        ArenaManageGUI arenaManageGUI = new ArenaManageGUI(this);
        Bukkit.getPluginManager().registerEvents(arenaManageGUI, this);

        getCommand("arena").setExecutor(new ArenaCommand(this, arenaManageGUI));
        getCommand("setcrate").setExecutor(new SetCrateCommand(this));
        getCommand("setdragonwaypoints").setExecutor(setDragonWaypointsCommand);
    }

    @Override
    public void onDisable() {
        for (Arena arena : arenaManager.getArenas()) {
            if (arena.getNPC() != null) {
                arena.getNPC().remove();
                arena.getNPCHologram().removeHologram();
            }
        }

        for (Arena arena : arenaManager.getArenas()) {
            for (UUID uuid : arena.getPlayers()) {
                arena.removePlayer(Bukkit.getPlayer(uuid));
            }
        }
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public FileManager getFileManager() { return fileManager; }
}
