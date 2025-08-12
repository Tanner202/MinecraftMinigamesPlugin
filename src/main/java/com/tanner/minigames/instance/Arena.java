package com.tanner.minigames.instance;

import com.google.common.collect.TreeMultimap;
import com.tanner.minigames.*;
import com.tanner.minigames.util.Constants;
import com.tanner.minigames.util.*;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.instance.game.colorswap.ColorSwapGame;
import com.tanner.minigames.instance.game.dragonescape.DragonEscapeGame;
import com.tanner.minigames.instance.game.scrapshuffle.ScrapyardSkirmish;
import com.tanner.minigames.instance.game.spleef.SpleefGame;
import com.tanner.minigames.instance.game.tntwars.TNTWarsGame;
import com.tanner.minigames.kit.*;
import com.tanner.minigames.manager.ConfigManager;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Arena {

    private Minigames minigames;
    private FileConfiguration config;

    private int id;
    private GameSettings gameSettings;
    private World world;
    private Villager npc;
    private Hologram npcHologram;
    private int worldUnloadWaitTime = 60;
    // The world load wait time must be longer than the unload wait time
    private int worldLoadWaitTime = 120;
    // This is the delay to set up the arena after the world has started loading
    private int setupDelay = 60;
    private boolean canJoin;

    private GameState state;
    private List<UUID> players = new ArrayList<>();
    private List<ScoreboardBuilder> scoreboardBuilders = new ArrayList<>();
    private HashMap<UUID, Team> teams = new HashMap<>();
    private HashMap<UUID, Kit> kits = new HashMap<>();
    private KitType[] availableKitTypes;
    private Team[] availableTeams;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames, int id, GameSettings gameSettings) {
        this.minigames = minigames;
        this.config = minigames.getConfig();

        this.id = id;
        this.gameSettings = gameSettings;
        world = gameSettings.getLobbySpawn().getWorld();

        this.state = GameState.RECRUITING;
        this.availableKitTypes = new KitType[0];
        this.availableTeams = Arrays.copyOfRange(Team.values(), 1, (gameSettings.getPlayerLimit() / Math.max(1, gameSettings.getTeamSize())) + 1);
        this.countdown = new Countdown(minigames, this);
        this.canJoin = true;

        Location npcSpawn = gameSettings.getNpcSpawn();
        if (npcSpawn != null) {
            npc = (Villager) npcSpawn.getWorld().spawnEntity(npcSpawn, EntityType.VILLAGER);
            npc.setAI(false);
            npc.setCollidable(false);
            npc.setInvulnerable(true);
            String[] hologramLines = new String[]{
                    ChatColor.YELLOW + ChatColor.BOLD.toString() + "CLICK HERE",
                    gameSettings.getGameType().getDisplayName(),
                    ChatColor.YELLOW + ChatColor.BOLD.toString() + getPlayers().size() + "/" + getPlayerLimit()
            };
            npcHologram = new Hologram(npcSpawn, hologramLines);
        }

        setGameType();
    }

    public void start() {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            player.setInvulnerable(false);
            player.getScoreboard().getObjective("lobby").unregister();
            player.closeInventory();
        }
        game.start();
    }

    public void reset(boolean kickPlayers) {
        setState(GameState.ENDING);
        if (kickPlayers) {

            while (!players.isEmpty()) {
                Player player = Bukkit.getPlayer(players.getFirst());
                removePlayer(player);
            }

            if (gameSettings.isWorldReloadEnabled()) {
                reloadWorld();
            }

            players.clear();
            teams.clear();
        }
        kits.clear();
        countdown.cancel();

        Bukkit.getScheduler().runTaskLater(minigames, () -> {
            countdown = new Countdown(minigames, this);
            setGameType();
            state = GameState.RECRUITING;
        }, worldLoadWaitTime + setupDelay);
    }

    private void reloadWorld() {
        canJoin = false;
        String worldName = world.getName();
        Bukkit.getScheduler().runTaskLater(minigames, () -> {
            Bukkit.unloadWorld(worldName, false);
        }, worldUnloadWaitTime);

        Bukkit.getScheduler().runTaskLater(minigames, () -> {
            world = Bukkit.createWorld(new WorldCreator(worldName));
            world.setAutoSave(false);
            gameSettings.getLobbySpawn().setWorld(world);
        }, worldLoadWaitTime);
    }

    private void setGameType() {
        switch (gameSettings.getGameType()) {
            case COLORSWAP:
                availableKitTypes = ColorSwapKitType.values();
                this.game = new ColorSwapGame(minigames, this);
                break;
            case TNT_WARS:
                availableKitTypes = TNTWarsKitType.values();
                this.game = new TNTWarsGame(minigames, this);
                break;
            case SPLEEF:
                this.game = new SpleefGame(minigames, this);
                break;
            case SCRAPYARD_SKIRMISH:
                this.game = new ScrapyardSkirmish(minigames, this);
                break;
            case DRAGON_ESCAPE:
                availableKitTypes = DragonEscapeKitType.values();
                this.game = new DragonEscapeGame(minigames, this);
                break;
        }
    }

    public void setDefaultKit(UUID uuid) {
        if (availableKitTypes.length == 0) return;

        setKit(uuid, availableKitTypes[0]);
    }

    public void setKit(UUID uuid, KitType type) {
        removeKit(uuid);

        kits.put(uuid, type.createKit(minigames, uuid));
        setPlayerScoreboard(Bukkit.getPlayer(uuid));
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
    public HashMap<UUID, Kit> getKits() { return kits; }

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

    public void playSound(Sound sound) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    private void setPlayerScoreboard(Player player) {
        HashMap<Integer, String> scoreboardLines = new HashMap<>();
        HashMap<Integer, ScoreboardTeam> scoreboardTeams = new HashMap<>();

        scoreboardLines.put(6, ChatColor.GRAY + "▶ Name: " + ChatColor.GREEN + player.getDisplayName());

        ScoreboardTeam team = new ScoreboardTeam("team", ChatColor.GRAY + "▶ Team: ",
                getTeam(player) != null ? getTeam(player).getDisplay() : "");
        scoreboardTeams.put(5, team);

        ScoreboardTeam kit = new ScoreboardTeam("kit", ChatColor.GRAY + "▶ Kit: ",
                getKit(player) != null ? getKit(player).getDisplay() : "");
        scoreboardTeams.put(4, kit);

        ScoreboardTeam playerAmount = new ScoreboardTeam("playerAmount", ChatColor.GRAY + "▶ Players: ",
                ChatColor.GREEN.toString() + players.size() + "/" + gameSettings.getPlayerLimit());
        scoreboardTeams.put(2, playerAmount);

        ScoreboardTeam arenaState = new ScoreboardTeam("arenaState", ChatColor.GRAY + "▶ State: ",
                ChatColor.GREEN + state.toString());
        scoreboardTeams.put(0, arenaState);

        ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder("lobby", ChatColor.AQUA.toString() + ChatColor.BOLD + "LOBBY",
                scoreboardLines, scoreboardTeams);
        player.setScoreboard(scoreboardBuilder.getBoard());
        scoreboardBuilders.add(scoreboardBuilder);
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.getInventory().clear();
        player.setInvulnerable(true);
        giveLobbyItems(player);
        setDefaultKit(player.getUniqueId());

        player.teleport(gameSettings.getLobbySpawn());

        TreeMultimap<Integer, Team> teamCount = TreeMultimap.create();
        for (Team team : availableTeams) {
            teamCount.put(getTeamCount(team), team);
        }

        Team lowestPlayerTeam = (Team) teamCount.values().toArray()[0];
        setTeam(player, lowestPlayerTeam);

        player.sendMessage(ChatColor.AQUA + "You have been automatically placed on " + lowestPlayerTeam.getDisplay() + ChatColor.AQUA + " team.");

        if (state.equals(GameState.RECRUITING) && players.size() >= ConfigManager.getRequiredPlayers()) {
            countdown.start();
        }

        if (state == GameState.RECRUITING || state == GameState.COUNTDOWN) {
            for (ScoreboardBuilder scoreboardBuilder : scoreboardBuilders) {
                scoreboardBuilder.updateScoreboard("playerAmount",
                        ChatColor.GREEN.toString() + players.size() + "/" + gameSettings.getPlayerLimit());
            }
        }

        updateNPCHologramPlayerCount();
    }

    public void removePlayer(Player player) {
        game.onPlayerLeave(player);
        players.remove(player.getUniqueId());
        player.getInventory().clear();
        player.setInvulnerable(false);
        removeKit(player.getUniqueId());
        removeTeam(player);
        player.teleport(ConfigManager.getLobbySpawn());
        player.sendTitle("", "");
        if (player.getScoreboard().getObjective("lobby") != null) {
            player.getScoreboard().getObjective("lobby").unregister();
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        TreeMultimap<Integer, Team> teamCount = TreeMultimap.create();
        for (Team team : availableTeams) {
            teamCount.put(getTeamCount(team), team);
        }

        Team highestPlayerTeam = (Team) teamCount.values().toArray()[teamCount.values().size() - 1];
        Team lowestPlayerTeam = (Team) teamCount.values().toArray()[0];
        if (getTeamCount(highestPlayerTeam) - 1 > getTeamCount(lowestPlayerTeam)) {
            for (UUID uuid : players) {
                Player _player = Bukkit.getPlayer(uuid);
                if (getTeam(_player) == highestPlayerTeam) {
                    setTeam(_player, lowestPlayerTeam);
                    _player.sendMessage(ChatColor.AQUA + "You have been moved to " + lowestPlayerTeam.getDisplay() + ChatColor.AQUA + " team to balance the teams.");
                    break;
                }
            }
        }

        if (state == GameState.RECRUITING || state == GameState.COUNTDOWN) {
            for (ScoreboardBuilder scoreboardBuilder : scoreboardBuilders) {
                scoreboardBuilder.updateScoreboard("playerAmount",
                        ChatColor.GREEN.toString() + players.size() + "/" + gameSettings.getPlayerLimit());
            }
        }

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "There are not enough players. Countdown stopped.");
            reset(false);
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "The game has ended because too many players have left.");
            game.end(false);
        }

        updateNPCHologramPlayerCount();
    }

    public void setTeam(Player player, Team team) {
        removeTeam(player);
        teams.put(player.getUniqueId(), team);
        setPlayerScoreboard(player);
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

    public Team[] getAvailableTeams() { return availableTeams; }

    public void save() {
        canJoin = false;
        for (Player p : world.getPlayers()) {
            p.teleport(ConfigManager.getLobbySpawn());
        }

        String worldName = world.getName();
        Bukkit.unloadWorld(worldName, true);
        World worldCopy = Bukkit.createWorld(new WorldCreator(worldName));
        worldCopy.setAutoSave(false);
    }

    private void giveLobbyItems(Player player) {
        ItemStack teamSelection = ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, ChatColor.GOLD + "Team Selection",
                Constants.TEAM_SELECTION, PersistentDataType.STRING, "TeamSelection");

        ItemStack kitSelection = ItemBuilder.createItem(Material.DIAMOND, ChatColor.BLUE + "Kit Selection",
                Constants.KIT_SELECTION, PersistentDataType.STRING, "KitSelection");

        ItemStack leaveItem = ItemBuilder.createItem(Material.RED_BED, ChatColor.RED + "Leave",
                Constants.LEAVE_ITEM, PersistentDataType.STRING, "LeaveItem");

        player.getInventory().setItem(0, teamSelection);
        player.getInventory().setItem(1, kitSelection);
        player.getInventory().setItem(8, leaveItem);
    }

    private void updateNPCHologramPlayerCount() {
        if (npcHologram != null) {
            npcHologram.update(2, ChatColor.YELLOW + ChatColor.BOLD.toString() + getPlayers().size() + "/" + getPlayerLimit());
        }
    }

    public int getId() { return id; }

    public GameState getState() { return state; }
    public void setState(GameState state) {
        this.state = state;

        if (state == GameState.RECRUITING || state == GameState.COUNTDOWN) {
            for (ScoreboardBuilder scoreboardBuilder : scoreboardBuilders) {
                scoreboardBuilder.updateScoreboard("arenaState",
                        ChatColor.GREEN + state.toString());
            }
        }
    }
    public Location getSpawn() { return gameSettings.getLobbySpawn(); }
    public void setLobbySpawn(Location spawn) {
        gameSettings.setLobbySpawn(spawn);
        world = spawn.getWorld();
        ConfigManager.setLocation("arenas." + id, spawn);
        minigames.saveConfig();
    }
    public Location getTeamSpawn(Team team) {
        FileConfiguration config = minigames.getConfig();
        String teamName = ChatColor.stripColor(team.getDisplay());
        String teamSpawnPath = "arenas." + id + ".team-spawns." + teamName.toLowerCase();
        String allTeamPath = "arenas." + id + ".team-spawns.all";

        if (config.contains(teamSpawnPath, true)) {
            return ConfigManager.getSpawn(teamSpawnPath);
        } else if (config.contains(allTeamPath, true)) {
            return ConfigManager.getSpawn(allTeamPath);
        }
        return null;
    }
    public void setTeamSpawn(Team team, Location spawn) {
        ConfigManager.setLocation("arenas." + id + ".team-spawns." + team.toString().toLowerCase(), spawn);
        minigames.saveConfig();
    }
    public World getWorld() { return world; }
    public boolean worldReloadEnabled() { return gameSettings.isWorldReloadEnabled(); }
    public void setWorldReloadEnabled(boolean worldReloadEnabled) {
        gameSettings.setWorldReloadEnabled(worldReloadEnabled);
        config.set("arenas." + id + ".world-reload-enabled", worldReloadEnabled);
        minigames.saveConfig();
    }
    public Villager getNPC() { return npc; }
    public void setNPCSpawn(Location spawn) {
        gameSettings.setNpcSpawn(spawn);
        ConfigManager.setLocation("arenas." + id + ".npc-spawn", spawn);
        minigames.saveConfig();
    }
    public Hologram getNPCHologram() { return npcHologram; }
    public int getTeamSize() { return gameSettings.getTeamSize(); }
    public void setTeamSize(int teamSize) {
        gameSettings.setTeamSize(teamSize);
        config.set("arenas." + id + ".team-size", teamSize);
        minigames.saveConfig();
    }
    public int getPlayerLimit() { return gameSettings.getPlayerLimit(); }
    public void setPlayerLimit(int playerLimit) {
        gameSettings.setPlayerLimit(playerLimit);
        updateNPCHologramPlayerCount();
        config.set("arenas." + id + ".max-players", playerLimit);
        minigames.saveConfig();
    }
    public boolean canJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }
    public GameType getGameType() { return gameSettings.getGameType(); }

    public List<UUID> getPlayers() { return players;}
}
