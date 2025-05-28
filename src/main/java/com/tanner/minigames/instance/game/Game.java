package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class Game implements Listener {

    protected Minigames minigames;
    protected Arena arena;

    public Game(Minigames minigames, Arena arena) {
        this.arena = arena;
        this.minigames = minigames;
    }

    public void start() {
        arena.setState(GameState.LIVE);
        Bukkit.getPluginManager().registerEvents(this, minigames);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().clear();
        }
        onStart();
    }

    public void end() {
        unregisterEvents();
        onEnd();
    }

    public abstract void onStart();
    public abstract void onEnd();

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}
