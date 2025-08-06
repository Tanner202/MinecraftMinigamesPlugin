package com.tanner.minigames.instance.game;

import com.tanner.minigames.instance.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class Game implements Listener {

    protected Minigames minigames;
    protected Arena arena;

    protected List<Player> winningPlayers;
    protected HashMap<Team, Location> teamSpawns = new HashMap<>();

    protected int arenaResetWaitTime = 200;

    public Game(Minigames minigames, Arena arena) {
        this.arena = arena;
        this.minigames = minigames;
        this.winningPlayers = new ArrayList<>();
    }

    public void start() {
        arena.setState(GameState.LIVE);
        Bukkit.getPluginManager().registerEvents(this, minigames);

        for (Team team : arena.getTeams()) {
            teamSpawns.put(team, arena.getTeamSpawn(team));
        }

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().clear();
            Kit kit = arena.getKits().get(uuid);
            if (kit != null) {
                kit.onStart(Bukkit.getPlayer(uuid));
            }

            Team team = arena.getTeam(player);
            Location teamSpawnLocation = teamSpawns.get(team);
            if (teamSpawnLocation != null) {
                player.teleport(teamSpawnLocation);
            }
        }
        onStart();
    }

    public void end(boolean gameComplete) {
        unregisterEvents();
        onEnd();

        for (UUID uuid : arena.getPlayers()) {
            arena.removeKit(uuid);
        }

        if (gameComplete) {
            VictoryCelebration celebration = new VictoryCelebration(minigames, this);
            Bukkit.getScheduler().runTaskLater(minigames, () -> {
                celebration.end();
                arena.reset(true);
            }, arenaResetWaitTime);
        } else {
            arena.reset(true);
        }
    }

    public abstract void onStart();
    public abstract void onEnd();
    public abstract void onPlayerRemoved(Player player);

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}
