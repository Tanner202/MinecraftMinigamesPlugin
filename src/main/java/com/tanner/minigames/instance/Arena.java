package com.tanner.minigames.instance;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.game.BlockBreakGame;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.instance.game.PVPGame;
import com.tanner.minigames.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena {

    private Minigames minigames;

    private int id;
    private Location spawn;
    private String gameName;

    private GameState state;
    private List<UUID> players;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames, int id, Location spawn, String game) {
        this.minigames = minigames;

        this.id = id;
        this.spawn = spawn;
        this.gameName = game;

        this.state = GameState.RECRUITING;
        this.players = new ArrayList<>();
        this.countdown = new Countdown(minigames, this);
        switch (gameName) {
            case "BLOCK":
                this.game = new BlockBreakGame(minigames, this);
                break;
            case "PVP":
                this.game = new PVPGame(minigames, this);
                break;
        }
    }

    public void start() {
        game.start();
    }

    public void reset(boolean kickPlayers) {
        if (kickPlayers) {
            Location loc = ConfigManager.getLobbySpawn();
            for (UUID uuid : players) {
                Bukkit.getPlayer(uuid).teleport(loc);
            }
            players.clear();
        }
        sendTitle("", "");
        state = GameState.RECRUITING;
        countdown.cancel();
        countdown = new Countdown(minigames, this);
        game.unregisterEvents();
        switch (gameName) {
            case "BLOCK":
                this.game = new BlockBreakGame(minigames, this);
                break;
            case "PVP":
                this.game = new PVPGame(minigames, this);
                break;
        }
    }

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendTitle(title, subtitle);
        }
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(spawn);

        if (state.equals(GameState.RECRUITING) && players.size() >= ConfigManager.getRequiredPlayers()) {
            countdown.start();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.teleport(ConfigManager.getLobbySpawn());
        player.sendTitle("", "");

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "There are not enough players. Countdown stopped.");
            reset(false);
            return;
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "The game has ended because too many players have left.");
            reset(false);
        }
    }

    public int getId() { return id; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Game getGame() { return game; }

    public List<UUID> getPlayers() { return players;}
}
