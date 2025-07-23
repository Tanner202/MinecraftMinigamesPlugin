package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.GameType;
import com.tanner.minigames.instance.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColorSwapGame extends Game {

    private Grid grid;
    private List<UUID> remainingPlayers;

    private int gridSize = 25;
    private int cellSize = 5;

    BukkitTask gameTimeTask;

    private int gameTimeElapsed = 0;
    private DifficultyStage[] difficultyStages = {
            new DifficultyStage(15, 5),
            new DifficultyStage(30, 4),
            new DifficultyStage(50, 3),
            new DifficultyStage(90, 2.5f),
            new DifficultyStage(150, 2.25f),
            new DifficultyStage(Integer.MAX_VALUE, 2f)
    };

    public ColorSwapGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        grid = new Grid(minigames, arena, arena.getSpawn(), gridSize, cellSize);
        remainingPlayers = new ArrayList<>();
    }

    @Override
    public void onStart() {
        remainingPlayers.addAll(arena.getPlayers());

        for (UUID uuid : arena.getPlayers()) {
            setScoreboard(Bukkit.getPlayer(uuid));
        }

        grid.start();
        gameTimeTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            gameTimeElapsed += 1;
            for (DifficultyStage stage : difficultyStages) {
                if (stage.stageEndTime < gameTimeElapsed) {
                    grid.setSwapInterval((long) stage.interval);
                }
            }
        }, 0, 20);
    }

    @Override
    public void onEnd() {
        grid.Stop();
        remainingPlayers.clear();
        gameTimeTask.cancel();
    }

    @Override
    public void onPlayerRemoved(Player player) {
        resetScoreboard(player);
    }

    private void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("colorswap", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(GameType.COLORSWAP.getDisplayName());

        String padding = ChatColor.RESET + "      ";

        Score teamScore = obj.getScore(ChatColor.GRAY + "▶ Team: " + arena.getTeam(player).getDisplay() + padding);
        teamScore.setScore(3);

        Score kitScore = obj.getScore(ChatColor.GRAY + "▶ Kit: " + arena.getKit(player).getDisplay() + padding);
        kitScore.setScore(2);

        Score empty = obj.getScore("");
        empty.setScore(1);

        Team playersRemaining = board.registerNewTeam("playersRemaining");
        playersRemaining.addEntry(ChatColor.BLUE.toString());
        playersRemaining.setPrefix(ChatColor.GRAY + "▶ Players Left: ");
        playersRemaining.setSuffix(ChatColor.GREEN.toString() + remainingPlayers.size());
        obj.getScore(ChatColor.BLUE.toString()).setScore(0);

        player.setScoreboard(board);
    }

    private void updateScoreboard() {
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getScoreboard().getTeam("playersRemaining").setSuffix(ChatColor.GREEN.toString() + remainingPlayers.size());
        }
    }

    private void resetScoreboard(Player player) {
        if (arena.getState() != GameState.LIVE && arena.getState() != GameState.ENDING) return;

        player.getScoreboard().getObjective("colorswap").unregister();
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
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
                remainingPlayers.remove(player.getUniqueId());
                if (remainingPlayers.size() == 1) {
                    Player winningPlayer = Bukkit.getPlayer(remainingPlayers.get(0));
                    arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
                    winningPlayers.add(winningPlayer);
                    end(true);
                }
            });
            updateScoreboard();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena != null && remainingPlayers.contains(player.getUniqueId())) {
            Material blockAtPlayerLocation = e.getPlayer().getLocation().getBlock().getType();
            if (blockAtPlayerLocation == Material.WATER) {
                player.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (arena.getPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (arena.getPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (arena.getPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (arena.getPlayers().contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
