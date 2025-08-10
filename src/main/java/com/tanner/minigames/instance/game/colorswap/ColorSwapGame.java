package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.util.ScoreboardBuilder;
import com.tanner.minigames.util.ScoreboardTeam;
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
import java.util.HashMap;
import java.util.UUID;

public class ColorSwapGame extends Game {

    private Grid grid;

    private int gridSize = 25;
    private int cellSize = 5;

    private ScoreboardBuilder scoreboardBuilder;

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
    }

    @Override
    public void onStart() {
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
        gameTimeTask.cancel();
    }

    @Override
    public void onPlayerEliminated(Player player) {

    }

    @Override
    public void checkWinCondition() {
        if (activePlayers.size() == 1) {
            Player winningPlayer = Bukkit.getPlayer(activePlayers.get(0));
            arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
            winningPlayers.add(winningPlayer);
            end(true);
        }
    }

    private void setScoreboard(Player player) {
        HashMap<Integer, String> scoreboardLines = new HashMap<>();
        HashMap<Integer, ScoreboardTeam> scoreboardTeams = new HashMap<>();

        String padding = ChatColor.RESET + "      ";

        scoreboardLines.put(3, ChatColor.GRAY + "▶ Team: " + arena.getTeam(player).getDisplay() + padding);
        scoreboardLines.put(2, ChatColor.GRAY + "▶ Kit: " + arena.getKit(player).getDisplay() + padding);

        ScoreboardTeam scoreboardTeam = new ScoreboardTeam("playersRemaining", ChatColor.GRAY + "▶ Players Left: ",
                ChatColor.GREEN.toString() + activePlayers.size());
        scoreboardTeams.put(0, scoreboardTeam);

        scoreboardBuilder = new ScoreboardBuilder(arena.getGameType().toString(),
                ChatColor.BOLD + arena.getGameType().getDisplayName(), scoreboardLines, scoreboardTeams);
        player.setScoreboard(scoreboardBuilder.getBoard());
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
                playerEliminated(player.getUniqueId());
                checkWinCondition();
                scoreboardBuilder.updateScoreboard("playersRemaining", ChatColor.GREEN.toString() + activePlayers.size());
            });
        }
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
