package com.tanner.minigames.instance;

import com.google.common.collect.TreeMultimap;
import com.tanner.minigames.Constants;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.KitType;
import com.tanner.minigames.manager.ConfigManager;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Arena {

    private Minigames minigames;

    private Location spawn;
    private World world;
    private int maxPlayers;
    private int worldResetWaitTime = 60;
    private boolean canJoin;
    private boolean worldReloadEnabled;

    private GameState state;
    private HashMap<UUID, Team> teams;
    private HashMap<UUID, KitType> kits;
    private KitType[] availableKitTypes;
    private Team[] availableTeams;
    private Countdown countdown;
    private Game game;

    public Arena(Minigames minigames) {

        FileConfiguration config = minigames.getConfig();

        spawn = new Location(
                Bukkit.getWorld(config.getString("arena.world")),
                config.getDouble("arena.x"),
                config.getDouble("arena.y"),
                config.getDouble("arena.z"),
                (float) config.getDouble("arena.yaw"),
                (float) config.getDouble("arena.pitch"));

        int numberOfTeams = config.getInt("arena.amount-of-teams");
        this.maxPlayers = config.getInt("arena.max-players");
        this.worldReloadEnabled = config.getBoolean("arena.world-reload-enabled");

        this.minigames = minigames;

        world = spawn.getWorld();

        this.state = GameState.RECRUITING;
        this.teams = new HashMap<>();
        this.kits = new HashMap<>();
        this.availableTeams = Arrays.copyOf(Team.values(), numberOfTeams);
        this.countdown = new Countdown(minigames, this);
        this.canJoin = true;
        this.game = new Game(minigames, this);
    }

    public void start() {
        game.start();
    }

    public void reset() {
        kits.clear();
        sendTitle("", "");
        state = GameState.RECRUITING;
        countdown.cancel();
        countdown = new Countdown(minigames, this);
        game.end();
        game = new Game(minigames, this);

        if (Bukkit.getOnlinePlayers().size() >= ConfigManager.getRequiredPlayers()) {
            countdown.start();
        }
    }

    private void reloadWorld() {
        canJoin = false;
        Bukkit.getScheduler().runTaskLater(minigames, () -> {
            String worldName = world.getName();
            Bukkit.unloadWorld(worldName, false);

            World worldCopy = Bukkit.createWorld(new WorldCreator(worldName));
            worldCopy.setAutoSave(false);
        }, worldResetWaitTime);
    }

    public void setKit(UUID uuid, KitType type) {
        removeKit(uuid);

        kits.put(uuid, type);
    }

    public void removeKit(UUID uuid) {
        kits.remove(uuid);
    }

    public KitType getKit(Player player) {
        return kits.containsKey(player.getUniqueId()) ? kits.get(player.getUniqueId()) : null;
    }

    public KitType[] getKitTypes() { return availableKitTypes; }
    public void setKitTypes(KitType[] availableKitTypes) {
        System.out.println("SET KIT TYPES <----------------------------");
        this.availableKitTypes = availableKitTypes.clone();
        for (KitType kitType : availableKitTypes) {
            System.out.println("KIT NAME: " + kitType.getName());
        }
        for (KitType kitType : this.availableKitTypes) {
            System.out.println("KIT NAME2: " + kitType.getName());
        }
        Bukkit.broadcastMessage("Available kit types: " + this.availableKitTypes.toString());
    }
    public HashMap<UUID, KitType> getKits() { return kits; }

    public void sendMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle);
        }
    }

    public void addPlayer(Player player) {
        player.getInventory().clear();
        giveLobbyItems(player);

        player.teleport(spawn);

        TreeMultimap<Integer, Team> teamCount = TreeMultimap.create();
        for (Team team : availableTeams) {
            teamCount.put(getTeamCount(team), team);
        }

        Team lowestPlayerTeam = (Team) teamCount.values().toArray()[0];
        setTeam(player, lowestPlayerTeam);

        player.sendMessage(ChatColor.AQUA + "You have been automatically placed on " + lowestPlayerTeam.getDisplay() + ChatColor.AQUA + " team.");

        if (state.equals(GameState.RECRUITING) && Bukkit.getOnlinePlayers().size() >= ConfigManager.getRequiredPlayers()) {
            countdown.start();
        }
    }

    public void removePlayer(Player player) {
        player.getInventory().clear();
        removeKit(player.getUniqueId());

        if (state == GameState.COUNTDOWN && Bukkit.getOnlinePlayers().size() - 1 < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "There are not enough players. Countdown stopped.");
            reset();
            return;
        }

        if (state == GameState.LIVE && Bukkit.getOnlinePlayers().size() - 1 < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "The game has ended because too many players have left.");
            reset();
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


    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Location getSpawn() { return spawn; }
    public World getWorld() { return world; }
    public boolean worldReloadEnabled() { return worldReloadEnabled; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean canJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }
}
