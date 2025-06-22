package com.tanner.minigames.instance.game.spleef;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import com.tanner.minigames.team.Team;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpleefGame extends Game {

    private List<UUID> remainingPlayers;
    private HashMap<Team, Location> teamSpawns;

    public SpleefGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        remainingPlayers = new ArrayList<>();
        this.teamSpawns = new HashMap<>();
    }

    @Override
    public void onStart() {
        for (Team team : arena.getTeams()) {
            teamSpawns.put(team, getTeamSpawn(team));
        }

        int count = 0;
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
            ItemMeta shovelMeta = shovel.getItemMeta();
            shovelMeta.setUnbreakable(true);
            shovel.setItemMeta(shovelMeta);
            player.getInventory().addItem(shovel);

            Team team = arena.getTeam(player);
            Location teamSpawnLocation = teamSpawns.get(team);
            player.teleport(teamSpawnLocation);

            remainingPlayers.add(uuid);
            count++;
        }
    }

    @Override
    public void onEnd() {
        remainingPlayers.clear();
    }

    @Override
    public void onPlayerRemoved(Player player) {

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


    public List<UUID> getRemainingPlayers() {
        return remainingPlayers;
    }

    public void removeActivePlayer(UUID uuid) {
        remainingPlayers.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        arena.sendMessage(ChatColor.RED + player.getDisplayName() + " was eliminated! " + remainingPlayers.size() + " players remain.");
        if (remainingPlayers.size() <= 1) {
            end(true);
        }
    }

    private boolean isInGame(Player player) {
        return getRemainingPlayers().contains(player.getUniqueId());
    }

    private boolean isInArena(Player player) {
        return arena.getPlayers().contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (!isInGame(e.getPlayer())) return;

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
        if (e.getEntity() instanceof Player && !isInGame((Player) e.getEntity())) return;

        if (e.getHitBlock() != null && e.getHitBlock().getType().equals(Material.SNOW_BLOCK)) {
            e.getHitBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (isInArena(player)) {
            if (isInGame(player)) {
                removeActivePlayer(player.getUniqueId());
            }
            arena.removePlayer(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && !isInGame((Player) e.getEntity())) return;

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
                remainingPlayers.remove(player.getUniqueId());
                if (remainingPlayers.size() == 1) {
                    Player winningPlayer = Bukkit.getPlayer(remainingPlayers.get(0));
                    arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
                    winningPlayers.add(winningPlayer);
                    end(true);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena != null && remainingPlayers.contains(player.getUniqueId())) {
            Material blockAtPlayerLocation = e.getPlayer().getLocation().getBlock().getType();
            if (blockAtPlayerLocation == Material.LAVA) {
                player.setHealth(0);
            }
        }
    }
}
