package com.tanner.minigames.instance.game.scrapshuffle;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class ScrapyardSkirmish extends Game {

    private HashMap<Team, Location> teamSpawns;
    private List<UUID> remainingPlayers;
    private List<Wall> walls;
    private List<Crate> crates;
    private YamlConfiguration wallsFile;
    private YamlConfiguration crateLocationsFile;
    private CrateData crateData;
    private int wallTimer;

    public ScrapyardSkirmish(Minigames minigames, Arena arena) {
        super(minigames, arena);
        remainingPlayers = new ArrayList<>();
        teamSpawns = new HashMap<>();
        walls = new ArrayList<>();
        crates = new ArrayList<>();

        crateData = new CrateData(getFile("crates.yml"));
        crateLocationsFile = getFile("crate_locations.yml");
        wallsFile = getFile("walls.yml");
        for (String wallID : wallsFile.getConfigurationSection("wall-locations").getKeys(false)) {
            Wall wall = new Wall(getBlockLoc(wallsFile, "wall-locations." + wallID + ".start"), getBlockLoc(wallsFile, "wall-locations." + wallID + ".end"));
            walls.add(wall);
        }
        wallTimer = wallsFile.getInt("wall-drop-time") * 20;

        removeCrates();
    }

    @Override
    public void onArenaReset() {

    }

    private YamlConfiguration getFile(String fileName) {
        File file = minigames.getFileManager().getFile(Paths.get("scrapyard_skirmish", fileName));
        if (file != null) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return null;
    }

    private Location getBlockLoc(YamlConfiguration file, String path) {
        return new Location(Bukkit.getWorld(file.getString(path + ".world")),
                file.getDouble(path + ".x"),
                file.getDouble(path + ".y"),
                file.getDouble(path + ".z"));
    }

    @Override
    public void onStart() {
        for (Team team : arena.getTeams()) {
            teamSpawns.put(team, getTeamSpawn(team));
        }

        arena.sendTitle(ChatColor.GREEN + "Game Has Started!", "Loot nearby crates for the first minute.");

        for (UUID uuid : arena.getPlayers()) {
            remainingPlayers.add(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.closeInventory();

            Team team = arena.getTeam(player);
            Location teamSpawnLocation = teamSpawns.get(team);
            player.teleport(teamSpawnLocation);
        }

        spawnCrates();

        Bukkit.getScheduler().runTaskLater(minigames, this::dropWalls, wallTimer);
    }

    private Location getTeamSpawn(Team team) {
        FileConfiguration config = minigames.getConfig();
        String teamName = ChatColor.stripColor(team.getDisplay());
        String teamSpawnPath = "arenas." + arena.getId() + ".team-spawns." + teamName.toLowerCase();
        return new Location(
                Bukkit.getWorld(config.getString(teamSpawnPath + ".world")),
                config.getDouble( teamSpawnPath + ".x"),
                config.getDouble(teamSpawnPath + ".y"),
                config.getDouble(teamSpawnPath + ".z"),
                (float) config.getDouble(teamSpawnPath + ".yaw"),
                (float) config.getDouble(teamSpawnPath + ".pitch"));
    }

    private void spawnCrates() {
        for (Team team : arena.getTeams()) {
            String teamName = ChatColor.stripColor(team.getDisplay().toLowerCase());
            List<String> availableCrateIds = new ArrayList<>(crateLocationsFile.getConfigurationSection(teamName).getKeys(false));

            for (int i = 0; i < crateData.getCrateAmount(); i++) {
                int randomIndex = new Random().nextInt(0, availableCrateIds.size());
                String randomCrateID = availableCrateIds.get(randomIndex);
                availableCrateIds.remove(randomIndex);

                Block crateBlock = getBlockLoc(crateLocationsFile, teamName + "." + randomCrateID).getBlock();
                crateBlock.setType(Material.CHEST);
                BlockState blockState = crateBlock.getState();
                if (blockState instanceof Chest) {
                    Chest chest = (Chest) blockState;
                    chest.getBlockInventory().clear();
                    Crate crate = new Crate(crateData, chest);
                    crates.add(crate);
                }
            }
        }
    }

    private void removeCrates() {
        for (String teamName : crateLocationsFile.getConfigurationSection("").getKeys(false))
            for (String crateID : crateLocationsFile.getConfigurationSection(teamName).getKeys(false)) {
                Block crateBlock = getBlockLoc(crateLocationsFile, teamName + "." + crateID).getBlock();
                crateBlock.setType(Material.AIR);
            }
        }

    private void dropWalls() {
        arena.sendTitle(ChatColor.GREEN + "The walls have dropped!", "You can now fight other players. Last team standing wins!");
        arena.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
        for (Wall wall : walls) {
            Location start = wall.getStart();
            Location end = wall.getEnd();

            double minX = Math.min(start.getX(), end.getX());
            double maxX = Math.max(start.getX(), end.getX());
            double minY = Math.min(start.getY(), end.getY());
            double maxY = Math.max(start.getY(), end.getY());
            double minZ = Math.min(start.getZ(), end.getZ());
            double maxZ = Math.max(start.getZ(), end.getZ());

            for (double x = minX; x <= maxX; x++) {
                for (double y = minY; y <= maxY; y++) {
                    for (double z = minZ; z <= maxZ; z++) {
                        Location location = new Location(arena.getWorld(), x, y, z);
                        if (location.getBlock().getType().equals(Material.GLASS)) {
                            location.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnd() {

    }
}
