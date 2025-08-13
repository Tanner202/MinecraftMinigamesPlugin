package com.tanner.minigames.instance.game.spleef;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.util.ScoreboardBuilder;
import com.tanner.minigames.util.ScoreboardTeam;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.UUID;

public class SpleefGame extends Game {

    private ScoreboardBuilder scoreboardBuilder;

    public SpleefGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        this.teamSpawns = new HashMap<>();
    }

    @Override
    public void onStart() {
        Scoreboard board = setScoreboard();
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
            ItemMeta shovelMeta = shovel.getItemMeta();
            shovelMeta.setUnbreakable(true);
            shovel.setItemMeta(shovelMeta);
            player.getInventory().addItem(shovel);

            Scoreboard board = setScoreboard();
            player.setScoreboard(board);
        }
    }

    @Override
    public void onEnd() {
        scoreboardBuilder.unregister();
    }

    @Override
    public void onPlayerEliminated(Player player) {
        scoreboardBuilder.updateScoreboard(player.getName(), ChatColor.RED + player.getDisplayName());
        checkWinCondition();
    }

    @Override
    public void onPlayerLeave(Player player) {

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

        scoreboardBuilder = new ScoreboardBuilder(arena.getGameType().toString(), arena.getGameType().getDisplayName(),
                scoreboardLines, scoreboardTeams);

        return scoreboardBuilder.getBoard();
    }

    private boolean isInArena(Player player) {
        return arena.getPlayers().contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (!isPlayerActive(e.getPlayer())) return;

        if (!e.getBlock().getType().equals(Material.SNOW_BLOCK)) {
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot break this block.");
            return;
        }

        e.setDropItems(false);
        ItemStack snowball = new ItemStack(Material.SNOWBALL);
        e.getPlayer().getInventory().addItem(snowball);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Player && !isPlayerActive((Player) e.getEntity())) return;

        if (e.getHitBlock() != null && e.getHitBlock().getType().equals(Material.SNOW_BLOCK)) {
            e.getHitBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (isInArena(player)) {
            if (isPlayerActive(player)) {
                activePlayers.remove(player.getUniqueId());
            }
            player.getScoreboard().getObjective(arena.getGameType().toString().toLowerCase()).unregister();
            arena.removePlayer(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && !isPlayerActive((Player) e.getEntity())) return;

        if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player && !isInArena((Player) e.getEntity())) return;

        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
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
                playerEliminated(player.getUniqueId());
            });
        }
        e.setDeathMessage("");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena != null && activePlayers.contains(player.getUniqueId())) {
            Material blockAtPlayerLocation = e.getPlayer().getLocation().getBlock().getType();
            if (blockAtPlayerLocation == Material.LAVA) {
                player.setHealth(0);
            }
        }
    }
}
