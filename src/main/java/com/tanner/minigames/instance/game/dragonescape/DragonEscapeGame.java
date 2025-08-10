package com.tanner.minigames.instance.game.dragonescape;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.util.ScoreboardBuilder;
import com.tanner.minigames.util.ScoreboardTeam;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.nio.file.Paths;
import java.util.*;

public class DragonEscapeGame extends Game {

    private YamlConfiguration file;
    private Location dragonSpawnLocation;
    private CustomEnderDragon customDragon;
    private Random random;
    private float minBlockThrowPower = 0.1f;
    private float maxBlockThrowPower = 0.5f;
    private long timeMillisSinceLastThrownBlock;
    private long timeMillisBetweenBlockThrows = 250;
    private ScoreboardBuilder scoreboardBuilder;

    public DragonEscapeGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        file = YamlConfiguration.loadConfiguration(minigames.getFileManager().getFile(Paths.get("dragon_escape/dragon_locations.yml")));
        dragonSpawnLocation = getDragonSpawn(file);
        random = new Random();
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

        Scoreboard board = setScoreboard();

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.setInvisible(true);
            player.setScoreboard(board);
        }
    }

    private Scoreboard setScoreboard() {

        HashMap<Integer, String> scoreboardLines = new HashMap<>();
        HashMap<Integer, ScoreboardTeam> scoreboardTeams = new HashMap<>();

        int count = 0;
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            ScoreboardTeam scoreboardTeam = new ScoreboardTeam(player.getName(), "", ChatColor.GREEN + player.getDisplayName());
            scoreboardTeams.put(count, scoreboardTeam);
            count++;
        }

        scoreboardBuilder = new ScoreboardBuilder(arena.getGameType().toString(),
                ChatColor.BOLD + arena.getGameType().getDisplayName(),
                scoreboardLines,
                scoreboardTeams);

        scoreboardBuilder.disablePlayerCollision();

        return scoreboardBuilder.getBoard();
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

    }

    @Override
    public void onPlayerEliminated(Player player) {
        player.setInvisible(false);

    }

    @Override
    public void checkWinCondition() {
        if (activePlayers.size() == 1) {
            Player winningPlayer = Bukkit.getPlayer(activePlayers.get(0));
            victory(winningPlayer);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location belowPlayer = player.getLocation().subtract(0, 1, 0);
        Block blockBelowPlayer = belowPlayer.getBlock();
        if (blockBelowPlayer.getType().equals(Material.BEACON) && activePlayers.contains(player.getUniqueId())) {
            victory(player);
        }

        if (isPlayerActive(player)) {
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
                scoreboardBuilder.updateScoreboard(player.getName(), ChatColor.RED + player.getDisplayName());
                playerEliminated(player.getUniqueId());
                checkWinCondition();

                player.setInvisible(false);
            });
        }
    }

    @EventHandler
    public void onDragonBlockBreak(EntityExplodeEvent e) {
        if (e.getEntityType().equals(EntityType.ENDER_DRAGON) && System.currentTimeMillis() - timeMillisSinceLastThrownBlock >= timeMillisBetweenBlockThrows) {
            EnderDragon dragon = (EnderDragon) e.getEntity();
            timeMillisSinceLastThrownBlock = System.currentTimeMillis();

            for (Block block : e.blockList()) {
                FallingBlock fallingBlock = dragon.getWorld().spawnFallingBlock(dragon.getLocation(), block.getBlockData());
                float randXPower = random.nextFloat(minBlockThrowPower, maxBlockThrowPower);
                float randZPower = random.nextFloat(minBlockThrowPower, maxBlockThrowPower);
                fallingBlock.setVelocity(new Vector(randXPower, maxBlockThrowPower, randZPower));
            }
        }
    }

    private void victory(Player winningPlayer) {
        arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
        winningPlayers.add(winningPlayer);
        customDragon.remove(Entity.RemovalReason.DISCARDED);
        end(true);
    }
}
