package com.tanner.minigames.instance;

import com.google.common.collect.TreeMultimap;
import com.tanner.minigames.Constants;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.game.BlockBreakGame;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.instance.game.PVPGame;
import com.tanner.minigames.instance.game.colorswap.ColorSwapGame;
import com.tanner.minigames.instance.game.spleef.SpleefGame;
import com.tanner.minigames.instance.game.tntwars.TNTWarsGame;
import com.tanner.minigames.kit.ColorSwapKitType;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.KitType;
import com.tanner.minigames.kit.TNTWarsKitType;
import com.tanner.minigames.manager.ConfigManager;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Arena {

    private Minigames minigames;

    private int id;
    private Location spawn;
    private World world;
    private Villager npc;
    private Location npcSpawn;
    private String gameName;
    private int maxPlayers;
    private int worldUnloadWaitTime = 60;
    // The world load wait time must be longer than the unload wait time
    private int worldLoadWaitTime = 120;
    // This is the delay to set up the arena after the world has started loading
    private int setupDelay = 60;
    private boolean canJoin;
    private boolean worldReloadEnabled;

    private GameState state;
    private List<UUID> players;
    private HashMap<UUID, Team> teams;
    private HashMap<UUID, Kit> kits;
    private KitType[] availableKitTypes;
    private Team[] availableTeams;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames, int id, Location spawn, String game, Location npcSpawn, int numberOfTeams, int maxPlayers, boolean worldReloadEnabled) {
        this.minigames = minigames;

        this.id = id;
        this.spawn = spawn;
        this.gameName = game;
        this.npcSpawn = npcSpawn;
        this.maxPlayers = maxPlayers;
        world = spawn.getWorld();
        this.worldReloadEnabled = worldReloadEnabled;

        this.state = GameState.RECRUITING;
        this.players = new ArrayList<>();
        this.teams = new HashMap<>();
        this.kits = new HashMap<>();
        this.availableKitTypes = new KitType[0];
        this.availableTeams = Arrays.copyOf(Team.values(), numberOfTeams);
        this.countdown = new Countdown(minigames, this);
        this.canJoin = true;

        if (npcSpawn != null) {
            npc = (Villager) npcSpawn.getWorld().spawnEntity(npcSpawn, EntityType.VILLAGER);
            npc.setAI(false);
            npc.setCollidable(false);
            npc.setInvulnerable(true);
            npc.setCustomNameVisible(true);
            npc.setCustomName(ChatColor.GREEN + "Arena " + id + ChatColor.GRAY + " (Click to Join)");
        }

        setGameType();
    }

    public void start() {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            player.setInvulnerable(false);
            player.closeInventory();
        }
        game.start();
    }

    public void reset(boolean kickPlayers) {
        if (kickPlayers) {
            Location loc = ConfigManager.getLobbySpawn();
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                player.getInventory().clear();
                player.teleport(loc);
                removeKit(uuid);
            }

            if (worldReloadEnabled) {
                reloadWorld();
            }

            players.clear();
            teams.clear();
        }
        kits.clear();
        sendTitle("", "");
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
            spawn.setWorld(world);
        }, worldLoadWaitTime);
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
                availableKitTypes = ColorSwapKitType.values();
                this.game = new ColorSwapGame(minigames, this);
                break;
            case "TNTWARS":
                availableKitTypes = TNTWarsKitType.values();
                this.game = new TNTWarsGame(minigames, this);
                break;
            case "SPLEEF":
                this.game = new SpleefGame(minigames, this);
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

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.getInventory().clear();
        player.setInvulnerable(true);
        giveLobbyItems(player);

        player.teleport(spawn);

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
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.getInventory().clear();
        player.setInvulnerable(false);
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
            game.end(false);
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

        player.getInventory().setItem(0, teamSelection);
        player.getInventory().setItem(1, kitSelection);
    }

    public int getId() { return id; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Location getSpawn() { return spawn; }
    public World getWorld() { return world; }
    public boolean worldReloadEnabled() { return worldReloadEnabled; }
    public int getMaxPlayers() { return maxPlayers; }
    public Villager getNPC() { return npc; }
    public boolean canJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }

    public List<UUID> getPlayers() { return players;}
}
