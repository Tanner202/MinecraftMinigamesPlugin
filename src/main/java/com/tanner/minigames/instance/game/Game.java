package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

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
        onStart();
        arena.sendMessage(ChatColor.GREEN + "Game Has Started! Your objective is to break 20 blocks in the fastest time. Good Luck");
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
