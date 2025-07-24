package com.tanner.minigames.instance;

import com.google.common.collect.TreeMultimap;
import com.tanner.minigames.*;
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
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Arena {

    private Minigames minigames;

    private int id;
    private GameSettings gameSettings;
    private World world;
    private Villager npc;
    private Hologram npcHologram;
    private BossBar bossBar;
    private int worldUnloadWaitTime = 60;
    // The world load wait time must be longer than the unload wait time
    private int worldLoadWaitTime = 120;
    // This is the delay to set up the arena after the world has started loading
    private int setupDelay = 60;
    private boolean canJoin;

    private GameState state;
    private List<UUID> players;
    private HashMap<UUID, Team> teams;
    private HashMap<UUID, Kit> kits;
    private KitType[] availableKitTypes;
    private Team[] availableTeams;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames, int id, GameSettings gameSettings) {
        this.minigames = minigames;

        this.id = id;
        this.gameSettings = gameSettings;
        world = gameSettings.getLobbySpawn().getWorld();

        this.state = GameState.RECRUITING;
        this.players = new ArrayList<>();
        this.teams = new HashMap<>();
        this.kits = new HashMap<>();
        this.availableKitTypes = new KitType[0];
        this.availableTeams = Arrays.copyOf(Team.values(), gameSettings.getTeamAmount());
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
                    ChatColor.YELLOW + ChatColor.BOLD.toString() + getPlayers().size() + "/" + getMaxPlayers()
            };
            npcHologram = new Hologram(npcSpawn, hologramLines);
        }

        bossBar = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID);

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

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD + "LOBBY");

        Score name = obj.getScore(ChatColor.GRAY + "▶ Name: " + ChatColor.GREEN + player.getDisplayName());
        name.setScore(6);

        Score space = obj.getScore(" ");
        space.setScore(1);

        org.bukkit.scoreboard.Team playerAmount = board.registerNewTeam("player_amount");
        playerAmount.addEntry(ChatColor.BOLD.toString());
        playerAmount.setPrefix(ChatColor.GRAY + "▶ Players: ");
        playerAmount.setSuffix(ChatColor.GREEN.toString() + players.size() + "/" + gameSettings.getMaxPlayerAmount());
        obj.getScore(ChatColor.BOLD.toString()).setScore(2);

        Score space2 = obj.getScore("  ");
        space2.setScore(3);

        org.bukkit.scoreboard.Team arenaState = board.registerNewTeam("arena_state");
        arenaState.addEntry(ChatColor.DARK_GRAY.toString());
        arenaState.setPrefix(ChatColor.GRAY + "▶ State: ");
        arenaState.setSuffix(ChatColor.GREEN + state.toString());
        obj.getScore(ChatColor.DARK_GRAY.toString()).setScore(0);

        org.bukkit.scoreboard.Team team = board.registerNewTeam("team");
        team.addEntry(ChatColor.YELLOW.toString());
        team.setPrefix(ChatColor.GRAY + "▶ Team: ");
        if (getTeam(player) != null) {
            team.setSuffix(getTeam(player).getDisplay());
        } else {
            team.setSuffix("");
        }
        obj.getScore(ChatColor.YELLOW.toString()).setScore(5);

        org.bukkit.scoreboard.Team kit = board.registerNewTeam("kit");
        kit.addEntry(ChatColor.GREEN.toString());
        kit.setPrefix(ChatColor.GRAY + "▶ Kit: ");
        if (getKit(player) != null) {
            kit.setSuffix(getKit(player).getDisplay());
        } else {
            kit.setSuffix("");
        }
        obj.getScore(ChatColor.GREEN.toString()).setScore(4);

        player.setScoreboard(board);
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

        bossBar.addPlayer(player);
        updateNPCHologramPlayerCount();
    }

    public void removePlayer(Player player) {
        game.onPlayerRemoved(player);
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
            updateScoreboard();
        }

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "There are not enough players. Countdown stopped.");
            reset(false);
            return;
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "The game has ended because too many players have left.");
            game.end(false);
        }

        bossBar.removePlayer(player);
        updateNPCHologramPlayerCount();
    }

    private void updateScoreboard() {
        for (UUID uuid : getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            setPlayerScoreboard(player);
        }
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
        ItemStack teamSelection = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta teamSelectionMeta = teamSelection.getItemMeta();
        teamSelectionMeta.setDisplayName(ChatColor.GOLD + "Team Selection");
        teamSelectionMeta.getPersistentDataContainer().set(Constants.TEAM_SELECTION, PersistentDataType.STRING, "TeamSelection");
        teamSelection.setItemMeta(teamSelectionMeta);

        ItemStack kitSelection = new ItemStack(Material.DIAMOND);
        ItemMeta kitSelectionMeta = kitSelection.getItemMeta();
        kitSelectionMeta.setDisplayName(ChatColor.BLUE + "Kit Selection");
        kitSelectionMeta.getPersistentDataContainer().set(Constants.KIT_SELECTION, PersistentDataType.STRING, "KitSelection");
        kitSelection.setItemMeta(kitSelectionMeta);

        ItemStack leaveItem = new ItemStack(Material.RED_BED);
        ItemMeta leaveItemMeta = leaveItem.getItemMeta();
        leaveItemMeta.setDisplayName(ChatColor.RED + "Leave");
        leaveItemMeta.getPersistentDataContainer().set(Constants.LEAVE_ITEM, PersistentDataType.STRING, "LeaveItem");
        leaveItem.setItemMeta(leaveItemMeta);

        player.getInventory().setItem(0, teamSelection);
        player.getInventory().setItem(1, kitSelection);
        player.getInventory().setItem(8, leaveItem);
    }

    public void setBossBar(String title) {
        bossBar.setTitle(title);
    }

    private void updateNPCHologramPlayerCount() {
        npcHologram.update(2, ChatColor.YELLOW + ChatColor.BOLD.toString() + getPlayers().size() + "/" + getMaxPlayers());
    }

    public int getId() { return id; }

    public GameState getState() { return state; }
    public void setState(GameState state) {
        this.state = state;

        if (state == GameState.RECRUITING || state == GameState.COUNTDOWN) {
            updateScoreboard();
        }
    }
    public Location getSpawn() { return gameSettings.getLobbySpawn(); }
    public World getWorld() { return world; }
    public boolean worldReloadEnabled() { return gameSettings.isWorldReloadEnabled(); }
    public int getMaxPlayers() { return gameSettings.getMaxPlayerAmount(); }
    public Villager getNPC() { return npc; }
    public Hologram getNPCHologram() { return npcHologram; }
    public boolean canJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }

    public List<UUID> getPlayers() { return players;}
}
