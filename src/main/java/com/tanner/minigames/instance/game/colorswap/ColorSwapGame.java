package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.util.Constants;
import com.tanner.minigames.util.ScoreboardBuilder;
import com.tanner.minigames.util.ScoreboardTeam;
import com.tanner.minigames.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ColorSwapGame extends Game {

    private Grid grid;
    private BossBar bossBar;

    private int gridSize = 25;
    private int cellSize = 5;

    private GridColor gridColor;

    private long swapInterval = 5;
    private int timeRemaining;

    BukkitTask generateGridTask;
    BukkitTask removeGridTask;
    BukkitTask removeWoolCountdownTask;

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
        grid = new Grid(arena.getSpawn(), gridSize, cellSize);
    }

    @Override
    public void onStart() {
        bossBar = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            setScoreboard(player);
            bossBar.addPlayer(player);
        }

        changeColor();
        gameTimeTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            gameTimeElapsed += 1;
            for (DifficultyStage stage : difficultyStages) {
                if (stage.stageEndTime < gameTimeElapsed) {
                    swapInterval = (long) stage.interval;
                }
            }
        }, 0, 20);
    }

    private void chooseRandomWool() {
        Random random = new Random();
        int randomIndex = random.nextInt(0, GridColor.values().length);
        gridColor = GridColor.values()[randomIndex];

        arena.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Go to " + gridColor.getDisplay());
    }

    private void changeColor() {
        chooseRandomWool();
        grid.setGrid();
        removeGridTask = Bukkit.getScheduler().runTaskLater(minigames, this::removeColor, swapInterval * 20);
        ItemStack wool = new ItemStack(gridColor.getMaterial(), 1);
        ItemMeta woolMeta = wool.getItemMeta();
        woolMeta.setEnchantmentGlintOverride(true);
        woolMeta.getPersistentDataContainer().set(Constants.WOOL, PersistentDataType.STRING, "Wool");
        wool.setItemMeta(woolMeta);

        timeRemaining = (int) swapInterval;
        removeWoolCountdownTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            bossBar.setTitle(gridColor.getColorCode() + Util.repeat("⬛", timeRemaining) + gridColor.getDisplay() + gridColor.getColorCode() + Util.repeat("⬛", timeRemaining));
            timeRemaining--;
        }, 0, 20);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null || item.getItemMeta().getPersistentDataContainer().has(Constants.WOOL)) {
                    player.getInventory().setItem(i, wool);
                }
            }
        }
    }

    private void removeColor() {
        generateGridTask = Bukkit.getScheduler().runTaskLater(minigames, this::changeColor, swapInterval * 20);
        grid.removeUnchosenWool(gridColor.getMaterial());
        removeWoolCountdownTask.cancel();
        bossBar.setTitle(gridColor.getDisplay());
    }

    @Override
    public void onEnd() {
        scoreboardBuilder.unregister();
        stop();
        gameTimeTask.cancel();
    }

    public void stop() {
        if (generateGridTask != null) {
            generateGridTask.cancel();
        }

        if (removeGridTask != null) {
            removeGridTask.cancel();
        }
    }


    @Override
    public void onPlayerEliminated(Player player) {

    }

    @Override
    public void onPlayerLeave(Player player) {
        bossBar.removePlayer(player);
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
