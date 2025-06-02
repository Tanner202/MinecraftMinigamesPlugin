package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.Kit;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Game implements Listener {

    protected Minigames minigames;
    protected Arena arena;

    protected List<Player> winningPlayers;

    protected int arenaResetWaitTime = 100;
    private int celebrationFireworkInterval = 20;
    private BukkitTask celebrationTask;

    public Game(Minigames minigames, Arena arena) {
        this.arena = arena;
        this.minigames = minigames;
        this.winningPlayers = new ArrayList<>();
    }

    public void start() {
        arena.setState(GameState.LIVE);
        Bukkit.getPluginManager().registerEvents(this, minigames);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().clear();
            Kit kit = arena.getKits().get(uuid);
            if (kit != null) {
                kit.onStart(Bukkit.getPlayer(uuid));
            }
        }
        onStart();
    }

    public void end(boolean gameComplete) {
        unregisterEvents();
        onEnd();
        if (gameComplete) {
            victoryCelebration();
            Bukkit.getScheduler().runTaskLater(minigames, () -> {
                celebrationTask.cancel();
                arena.reset(true);
            }, arenaResetWaitTime);
        } else {
            arena.reset(true);
        }
    }

    protected void victoryCelebration() {
        celebrationTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            for (Player player : winningPlayers) {
                player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        }}, 0, celebrationFireworkInterval);
    }

    public abstract void onStart();
    public abstract void onEnd();

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}
