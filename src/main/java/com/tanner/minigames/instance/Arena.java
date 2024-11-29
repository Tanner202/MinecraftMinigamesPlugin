package com.tanner.minigames.instance;

import com.google.common.collect.TreeMultimap;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.game.BlockBreakGame;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.instance.game.PVPGame;
import com.tanner.minigames.instance.game.colorswap.ColorSwapGame;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.KitType;
import com.tanner.minigames.kit.TNTWarsKitType;
import com.tanner.minigames.manager.ConfigManager;
import com.tanner.minigames.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {

    private Minigames minigames;

    private int id;
    private Location spawn;
    private String gameName;
    private int playersPerTeam;
    private int maxPlayers;
    private int numberOfTeams;

    private GameState state;
    private List<UUID> players;
    private HashMap<UUID, Team> teams;
    private HashMap<UUID, Kit> kits;
    private KitType[] availableKitTypes;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames, int id, Location spawn, String game, int playersPerTeam, int maxPlayers) {
        this.minigames = minigames;

        this.id = id;
        this.spawn = spawn;
        this.gameName = game;
        this.playersPerTeam = playersPerTeam;
        this.maxPlayers = maxPlayers;
        this.numberOfTeams = maxPlayers/playersPerTeam;

        this.state = GameState.RECRUITING;
        this.players = new ArrayList<>();
        this.teams = new HashMap<>();
        this.kits = new HashMap<>();
        this.countdown = new Countdown(minigames, this);

        setGameType();
    }

    public void start() {
        game.start();
    }

    public void reset(boolean kickPlayers) {
        if (kickPlayers) {
            Location loc = ConfigManager.getLobbySpawn();
            for (UUID uuid : players) {
                Bukkit.getPlayer(uuid).teleport(loc);
                removeKit(uuid);
            }
            players.clear();
            teams.clear();
        }
        sendTitle("", "");
        state = GameState.RECRUITING;
        countdown.cancel();
        countdown = new Countdown(minigames, this);
        game.unregisterEvents();
        setGameType();
    }

    private void setGameType() {
        switch (gameName) {
            case "BLOCK":
                this.game = new BlockBreakGame(minigames, this);
                break;
            case "PVP":
                this.game = new PVPGame(minigames, this);
                break;
            case "COLORSWAP":
                this.game = new ColorSwapGame(minigames, this);
                break;
            case "TNTWARS":
                availableKitTypes = TNTWarsKitType.values();
                break;
        }
    }

    public void setKit(UUID uuid, KitType type) {
        removeKit(uuid);

        kits.put(uuid, type.createKit(minigames, uuid));
    }

    public void removeKit(UUID uuid) {
        if (kits.containsKey(uuid)) {
            kits.get(uuid).remove();
            kits.remove(uuid);
        }
    }

    public KitType getKit(Player player) {
        return kits.containsKey(player.getUniqueId()) ? kits.get(player.getUniqueId()).getType() : null;
    }

    public KitType[] getKitTypes() { return availableKitTypes; }

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

        TreeMultimap<Integer, Team> teamCount = TreeMultimap.create();
        Team[] teamValues = Team.values();
        for (int i = 0; i < numberOfTeams; i++) {
            Team team = teamValues[i];
            teamCount.put(getTeamCount(team), team);
        }

        Team lowestPlayerTeam = (Team) teamCount.values().toArray()[0];
        setTeam(player, lowestPlayerTeam);

        player.sendMessage(ChatColor.AQUA + "You have been automatically placed on " + lowestPlayerTeam.getDisplay() + ChatColor.AQUA + " team.");

        if (state.equals(GameState.RECRUITING) && players.size() >= ConfigManager.getRequiredPlayers()) {
            countdown.start();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        removeKit(player.getUniqueId());
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

    public void setTeam(Player player, Team team) {
        removeTeam(player);
        teams.put(player.getUniqueId(), team);
    }

    public void removeTeam(Player player) {
        if (teams.containsKey(player.getUniqueId())) {
            teams.remove(player.getUniqueId());
        }
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public int getTeamCount(Team team) {
        int amount = 0;
        for (Team t : teams.values()) {
            if (t == team) {
                amount++;
            }
        }
        return amount;
    }

    public Team getTeam(Player player) {
        return teams.get(player.getUniqueId());
    }

    public int getId() { return id; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Location getSpawn() { return spawn; }

    public List<UUID> getPlayers() { return players;}
}
