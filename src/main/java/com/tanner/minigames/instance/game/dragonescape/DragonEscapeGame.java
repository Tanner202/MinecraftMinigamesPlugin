package com.tanner.minigames.instance.game.dragonescape;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Team;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DragonEscapeGame extends Game {

    private YamlConfiguration file;
    private Location dragonSpawnLocation;
    private CustomEnderDragon customDragon;
    private List<UUID> alivePlayers = new ArrayList<>();

    public DragonEscapeGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        file = YamlConfiguration.loadConfiguration(minigames.getFileManager().getFile(Paths.get("dragon_escape/dragon_locations.yml")));
        dragonSpawnLocation = getDragonSpawn(file);
    }

    private Location getDragonSpawn(YamlConfiguration file) {
        String prefix = "dragon-spawn";
        return new Location(Bukkit.getWorld(file.getString(prefix + ".world")),
                file.getDouble(prefix + ".x"),
                file.getDouble(prefix + ".y"),
                file.getDouble(prefix + ".z"),
                (float) file.getDouble(prefix + ".yaw"),
                (float) file.getDouble(prefix + ".pitch"));
    }

    @Override
    public void onStart() {
        Vec3[] targets = getTargetLocations();
        try {
            customDragon = new CustomEnderDragon(((CraftWorld) arena.getWorld()).getHandle().getLevel(), dragonSpawnLocation, targets);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Team scoreboardTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("noCollision");
        if (scoreboardTeam == null) {
            scoreboardTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("noCollision");
        }
        scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            scoreboardTeam.addEntry(player.getName());
            player.setInvisible(true);

            com.tanner.minigames.team.Team team = arena.getTeam(player);
            Location teamSpawnLocation = getTeamSpawn(team);
            player.teleport(teamSpawnLocation);
        }

        alivePlayers.addAll(arena.getPlayers());
    }

    private Location getTeamSpawn(com.tanner.minigames.team.Team team) {
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

    private Vec3[] getTargetLocations() {
        int targetAmount = file.getConfigurationSection("target-locations").getKeys(false).size();
        Vec3[] targets = new Vec3[targetAmount];
        int currentTargetIndex = 0;
        String prefix = "target-locations.";
        for (String key : file.getConfigurationSection("target-locations").getKeys(false)) {
            double x = file.getDouble(prefix + key + ".x");
            double y = file.getDouble(prefix + key + ".y");
            double z = file.getDouble(prefix + key + ".z");
            Vec3 target = new Vec3(x, y, z);
            targets[currentTargetIndex] = target;
            currentTargetIndex++;
        }
        return targets;
    }

    @Override
    public void onEnd() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("noCollision");
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            team.removeEntry(player.getName());
            player.setInvisible(false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location belowPlayer = player.getLocation().subtract(0, 1, 0);
        Block blockBelowPlayer = belowPlayer.getBlock();
        if (blockBelowPlayer.getType().equals(Material.BEACON) && alivePlayers.contains(player.getUniqueId())) {
            victory(player);
        }

        if (arena != null && alivePlayers.contains(player.getUniqueId())) {
            Material blockAtPlayerLocation = e.getPlayer().getLocation().getBlock().getType();
            if (blockAtPlayerLocation == Material.WATER) {
                player.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (arena.getPlayers().contains(player.getUniqueId())) {
            minigames.getServer().getScheduler().scheduleSyncDelayedTask(minigames, () -> {
                if (player.isDead()) {
                    player.spigot().respawn();
                    player.teleport(arena.getSpawn());
                    player.sendTitle(ChatColor.RED + "You Died!", "");
                    player.setGameMode(GameMode.SPECTATOR);
                }
                alivePlayers.remove(player.getUniqueId());
                if (alivePlayers.size() == 1) {
                    Player winningPlayer = Bukkit.getPlayer(alivePlayers.get(0));
                    victory(winningPlayer);
                }

                player.setInvisible(false);
            });
        }
    }

    private void victory(Player winningPlayer) {
        arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
        winningPlayers.add(winningPlayer);
        customDragon.remove(Entity.RemovalReason.DISCARDED);
        end(true);
    }
}
