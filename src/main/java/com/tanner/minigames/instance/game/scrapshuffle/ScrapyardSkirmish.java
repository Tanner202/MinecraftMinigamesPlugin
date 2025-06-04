package com.tanner.minigames.instance.game.scrapshuffle;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ScrapyardSkirmish extends Game {

    private HashMap<Team, Location> teamSpawns;
    private List<UUID> remainingPlayers;
    private List<Wall> walls;
    private YamlConfiguration wallsFile;

    public ScrapyardSkirmish(Minigames minigames, Arena arena) {
        super(minigames, arena);
        remainingPlayers = new ArrayList<>();
        teamSpawns = new HashMap<>();
        walls = new ArrayList<>();
        File file = minigames.getFileManager().getFile("scrapyard_skirmish/walls.yml");
        if (file != null) {
            wallsFile = YamlConfiguration.loadConfiguration(file);

            for (String wallID : wallsFile.getKeys(false)) {
                Wall wall = new Wall(getWallLoc(wallID + ".start"), getWallLoc(wallID + ".end"));
                walls.add(wall);
            }
        }
    }

    private Location getWallLoc(String path) {
        return new Location(Bukkit.getWorld(wallsFile.getString(path + ".world")),
                wallsFile.getDouble(path + ".x"),
                wallsFile.getDouble(path + ".y"),
                wallsFile.getDouble(path + ".z"));
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
