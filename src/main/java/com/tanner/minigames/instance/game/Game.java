package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.event.GameEndEvent;
import com.tanner.minigames.event.GameStartEvent;
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
        Bukkit.getPluginManager().registerEvents(this, minigames);
    }

    public void start() {
        arena.setState(GameState.LIVE);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
        }
        GameStartEvent gameStartEvent = new GameStartEvent();
        Bukkit.getPluginManager().callEvent(gameStartEvent);
        onStart();
    }

    public void end() {
        unregisterEvents();
        GameEndEvent gameEndEvent = new GameEndEvent();
        Bukkit.getPluginManager().callEvent(gameEndEvent);
        onEnd();
    }

    public abstract void onStart();
    public abstract void onEnd();

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}
