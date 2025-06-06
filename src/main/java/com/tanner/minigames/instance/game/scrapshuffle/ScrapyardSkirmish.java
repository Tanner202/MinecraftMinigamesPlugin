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
import java.util.*;

public class ScrapyardSkirmish extends Game {

    private HashMap<Team, Location> teamSpawns;
    private List<UUID> remainingPlayers;
    private List<Wall> walls;
    private List<Crate> crates;
    private YamlConfiguration wallsFile;
    private YamlConfiguration crateLocationsFile;
    private CrateData crateData;

    public ScrapyardSkirmish(Minigames minigames, Arena arena) {
        super(minigames, arena);
        remainingPlayers = new ArrayList<>();
        teamSpawns = new HashMap<>();
        walls = new ArrayList<>();
        crates = new ArrayList<>();
        crateData = new CrateData(getFile("scrapyard_skirmish/crates.yml"));
        crateLocationsFile = getFile("scrapyard_skirmish/crate_locations.yml");
        wallsFile = getFile("scrapyard_skirmish/walls.yml");
        for (String wallID : this.wallsFile.getKeys(false)) {
            Wall wall = new Wall(getBlockLoc(wallsFile, wallID + ".start"), getBlockLoc(wallsFile, wallID + ".end"));
            walls.add(wall);
        }
    }

    private YamlConfiguration getFile(String path) {
        File file = minigames.getFileManager().getFile(path);
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
        setWalls(Material.GLASS);
        for (Team team : arena.getTeams()) {
            teamSpawns.put(team, getTeamSpawn(team));
        }

        arena.sendMessage(ChatColor.GREEN + "Game Has Started! Collect scrap for the first 5 minutes, then the walls " +
                "will drop and you will be able to fight other players. The last team standing will win!");

        for (UUID uuid : arena.getPlayers()) {
            remainingPlayers.add(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.closeInventory();

            Team team = arena.getTeam(player);
            Location teamSpawnLocation = teamSpawns.get(team);
            player.teleport(teamSpawnLocation);
        }

        spawnCrates();

        Bukkit.getScheduler().runTaskLater(minigames, this::dropWalls, 200);
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
        int crateAmount = 2;
        for (Team team : arena.getTeams()) {
            String teamName = ChatColor.stripColor(team.getDisplay().toLowerCase());
            List<String> availableCrateIds = new ArrayList<>(crateLocationsFile.getConfigurationSection(teamName).getKeys(false));
            for (int i = 0; i < crateAmount; i++) {
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

    private void dropWalls() {
        arena.sendMessage(ChatColor.GREEN + "The walls have dropped! You can now fight other players. Last team standing wins!");
        arena.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
        setWalls(Material.AIR);
    }

    private void setWalls(Material material) {
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
                        location.getBlock().setType(material);
                    }
                }
            }
        }
    }

    @Override
    public void onEnd() {

    }
}
