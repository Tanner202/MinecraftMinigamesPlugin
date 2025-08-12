package com.tanner.minigames.instance.game.tntwars;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.kit.TNTWarsKitType;
import com.tanner.minigames.team.Team;
import com.tanner.minigames.util.ScoreboardBuilder;
import com.tanner.minigames.util.ScoreboardTeam;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TNTWarsGame extends Game {

    private Minigames minigames;

    private Arena arena;
    private List<ScoreboardBuilder> scoreboardBuilders = new ArrayList<>();

    private int tntInterval = 200;
    private int snowballInterval = 25;

    private BukkitTask giveTntTask;
    private BukkitTask giveSnowballTask;

    public TNTWarsGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        this.arena = arena;
        this.minigames = minigames;
        this.teamSpawns = new HashMap<>();
    }

    @Override
    public void onStart() {
        arena.sendMessage(ChatColor.GREEN + "Game Has Started! Knock the other player off by launching TNT. Last team standing wins!");

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            setScoreboard(player);

            player.setAllowFlight(true);
            player.setFlying(false);
        }

        giveTntTask = Bukkit.getScheduler().runTaskTimer(minigames, this::givePlayersTnt, 100, tntInterval);
        giveSnowballTask = Bukkit.getScheduler().runTaskTimer(minigames, this::givePlayersSnowball, 50, snowballInterval);
    }

    private void givePlayersTnt() {
        ItemStack throwableTnt = new ItemStack(Material.TNT, 1);
        String message = ChatColor.GREEN + "+1 Throwable Tnt";
        Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

        givePlayersItem(throwableTnt, message, sound);
    }

    private void givePlayersSnowball() {
        ItemStack explosiveSnowball = new ItemStack(Material.SNOWBALL, 1);

        givePlayersItem(explosiveSnowball);
    }

    private void givePlayersItem(ItemStack item) {
        for (UUID uuid : activePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().addItem(item);
        }
    }

    private void givePlayersItem(ItemStack item, String message, Sound sound) {
        for (UUID uuid : activePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().addItem(item);

            player.sendMessage(message);
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

    private void setScoreboard(Player player) {
        HashMap<Integer, String> scoreboardLines = new HashMap<>();
        HashMap<Integer, ScoreboardTeam> scoreboardTeams = new HashMap<>();

        String padding = ChatColor.RESET + "      ";

        scoreboardLines.put(4, ChatColor.GRAY + "▶ Team: " + arena.getTeam(player).getDisplay() + padding);
        scoreboardLines.put(3, ChatColor.GRAY + "▶ Kit: " + arena.getKit(player).getDisplay() + padding);

        ScoreboardTeam blueTeam = new ScoreboardTeam("blue", ChatColor.BLUE + "▶ Blue: ", ChatColor.GRAY.toString() + arena.getTeamCount(Team.BLUE));
        scoreboardTeams.put(1, blueTeam);

        ScoreboardTeam redTeam = new ScoreboardTeam("red", ChatColor.RED + "▶ Red: ", ChatColor.GRAY.toString() + arena.getTeamCount(Team.RED));
        scoreboardTeams.put(0, redTeam);

        ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder(arena.getGameType().toString(),
                arena.getGameType().getDisplayName(), scoreboardLines, scoreboardTeams);

        player.setScoreboard(scoreboardBuilder.getBoard());
        scoreboardBuilders.add(scoreboardBuilder);
    }

    @Override
    public void onEnd() {
        for (ScoreboardBuilder scoreboardBuilder : scoreboardBuilders) {
            scoreboardBuilder.unregister();
        }
        giveTntTask.cancel();
        giveSnowballTask.cancel();
    }

    @Override
    public void onPlayerEliminated(Player player) {
        HashMap<Team, Integer> remainingPlayersPerTeam = getRemainingPlayersPerTeam();
        for (ScoreboardBuilder scoreboardBuilder : scoreboardBuilders) {
            scoreboardBuilder.updateScoreboard("blue", ChatColor.GRAY.toString() + remainingPlayersPerTeam.get(Team.BLUE));
            scoreboardBuilder.updateScoreboard("red", ChatColor.GRAY.toString() + remainingPlayersPerTeam.get(Team.RED));
        }
        checkWinCondition();
    }

    @Override
    public void onPlayerLeave(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    @Override
    public void checkWinCondition() {
        Team team = getWinningTeam();
        if (team != null) {
            arena.sendMessage(team.getDisplay() + ChatColor.GREEN + " Team has Won! Thanks for Playing!");
            for (UUID uuid : activePlayers) {
                winningPlayers.add(Bukkit.getPlayer(uuid));
            }
            end(true);
        }
    }

    private Team getWinningTeam() {
        Team winningTeam = null;
        for (Team team : arena.getTeams()) {
            HashMap<Team, Integer> remainingPlayersPerTeam = getRemainingPlayersPerTeam();
            if (remainingPlayersPerTeam.get(team) > 0) {
                if (winningTeam == null) {
                    winningTeam = team;
                }
                else
                {
                    return null;
                }
            }
        }
        return winningTeam;
    }

    private HashMap<Team, Integer> getRemainingPlayersPerTeam() {
        HashMap<Team, Integer> remainingPlayersPerTeam = new HashMap<>();
        for (Team team : arena.getTeams()) {
            remainingPlayersPerTeam.put(team, 0);
        }
        for (UUID uuid : activePlayers) {
            Team playerTeam = arena.getTeam(Bukkit.getPlayer(uuid));
            int remainingTeamPlayers = remainingPlayersPerTeam.get(playerTeam);
            remainingPlayersPerTeam.replace(playerTeam, remainingTeamPlayers, remainingTeamPlayers + 1);
        }
        return remainingPlayersPerTeam;
    }

    // ----------- Game Listener -----------
    private float tntLaunchPower = 1.5f;
    private float tntHeight = 0.5f;
    private int fuseTime = 45;

    private float snowballExplosionPower = 2f;
    private float snowballLaunchPower = 1.5f;
    private float snowballHeight = 0.25f;

    private float playerDoubleJumpPower = 1f;
    private float forwardPower = 1f;
    private long jumpCooldown = 3;
    private Cache<UUID, Long> doubleJumpCooldown = CacheBuilder.newBuilder().expireAfterWrite(jumpCooldown, TimeUnit.SECONDS).build();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isPlayerActive(player)) {
            if ((e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) && itemInMainHand.getType().equals(Material.TNT)) {
                itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);

                World world = player.getWorld();
                TNTPrimed tntPrimed = (TNTPrimed) world.spawnEntity(player.getEyeLocation(), EntityType.TNT);
                tntPrimed.setFuseTicks(fuseTime);
                Vector playerFacing = player.getEyeLocation().getDirection();

                Vector heightVector = new Vector(0, tntHeight, 0);
                tntPrimed.setVelocity(playerFacing.multiply(tntLaunchPower).add(heightVector));
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();

            if (isPlayerActive(player)) {
                World world = e.getEntity().getWorld();
                if (e.getEntity().getType().equals(EntityType.SNOWBALL)) {
                    if (e.getHitBlock() != null) {
                        Location hitLocation = e.getHitBlock().getLocation();
                        world.createExplosion(hitLocation, snowballExplosionPower, false, true);
                    } else if (e.getHitEntity() != null) {
                        Location hitLocation = e.getHitEntity().getLocation();
                        world.createExplosion(hitLocation, snowballExplosionPower, false, true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();

            if (isPlayerActive(player)) {
                if (e.getEntity().getType().equals(EntityType.SNOWBALL)) {
                    Vector playerFacing = player.getEyeLocation().getDirection();

                    Vector heightVector = new Vector(0, snowballHeight, 0);
                    e.getEntity().setVelocity(playerFacing.multiply(snowballLaunchPower).add(heightVector));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();

        if (isPlayerActive(player)) {
            Vector preEventVelocity = player.getVelocity();
            e.setCancelled(true);

            UUID playerUniqueId = player.getUniqueId();
            if (!doubleJumpCooldown.asMap().containsKey(playerUniqueId)) {
                doubleJump(player);
                doubleJumpCooldown.put(playerUniqueId, System.currentTimeMillis() + jumpCooldown * 1000);
                player.setAllowFlight(false);
            } else {
                long distance = doubleJumpCooldown.asMap().get(playerUniqueId) - System.currentTimeMillis();
                long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(distance);
                player.sendMessage(ChatColor.RED + "Your double jump is on cooldown for " + remainingSeconds +
                        " more second" + (remainingSeconds == 1 ? "" : "s"));
                player.setVelocity(preEventVelocity);
            }
        }
    }

    private void doubleJump(Player player) {
        Vector playerDirection = player.getLocation().getDirection();
        Vector doubleJumpVector = new Vector(playerDirection.getX() * forwardPower, playerDoubleJumpPower,
                playerDirection.getZ() * forwardPower);
        player.setVelocity(doubleJumpVector);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

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

        if (isPlayerActive(player)) {
            minigames.getServer().getScheduler().scheduleSyncDelayedTask(minigames, () -> {
                if (player.isDead()) {
                    playerEliminated(player.getUniqueId());
                    player.spigot().respawn();
                    player.sendTitle(ChatColor.RED + "You Died!", "");
                    player.teleport(arena.getSpawn());
                }
            });
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        if (isPlayerActive(player)) {
            if (e.getBlock().getType().equals(Material.TNT)) {
                player.sendMessage(ChatColor.RED + "You cannot place this block.");
                e.setCancelled(true);
            }

            if (!arena.getKit(player).equals(TNTWarsKitType.BUILDER)) {
                player.sendMessage(ChatColor.RED + "You cannot place blocks unless using the builder kit.");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (!isPlayerActive(player)) return;

        if (!arena.getKit(player).equals(TNTWarsKitType.BUILDER)) {
            player.sendMessage(ChatColor.RED + "You cannot break blocks unless using the builder kit.");
            e.setCancelled(true);
        }
    }
}
