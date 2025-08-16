package com.tanner.minigames.instance.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GameEventControl implements Listener {

    private List<UUID> arenaPlayers;
    private List<UUID> activePlayers;
    private List<GameEventFlag> gameEventFlags;

    public GameEventControl(List<UUID> arenaPlayers, List<UUID> activePlayers, GameEventFlag... gameEventFlags) {
        this.arenaPlayers = arenaPlayers;
        this.activePlayers = activePlayers;
        this.gameEventFlags = Arrays.asList(gameEventFlags);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (activePlayers.contains(player.getUniqueId()) && gameEventFlags.contains(GameEventFlag.WATER_DAMAGE)) {
            Material blockAtPlayerLocation = e.getPlayer().getLocation().getBlock().getType();
            if (blockAtPlayerLocation == Material.WATER) {
                player.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (arenaPlayers.contains(e.getPlayer().getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_ITEM_DROP)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (arenaPlayers.contains(e.getPlayer().getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_BLOCK_BREAK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (arenaPlayers.contains(e.getPlayer().getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_BLOCK_PLACE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (arenaPlayers.contains(player.getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_INVENTORY_INTERACTION)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !((e.getDamager()) instanceof Player)) return;

        Bukkit.broadcastMessage("Both players");
        Player hitPlayer = (Player) e.getEntity();
        Player attackingPlayer = (Player) e.getDamager();

        if (activePlayers.contains(hitPlayer.getUniqueId()) && activePlayers.contains(attackingPlayer.getUniqueId())
                && gameEventFlags.contains(GameEventFlag.DISABLE_PVP)) {
            Bukkit.broadcastMessage("active");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if (arenaPlayers.contains(player.getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_HUNGER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        for (HumanEntity humanEntity : e.getViewers()) {
            if (arenaPlayers.contains(humanEntity.getUniqueId()) && gameEventFlags.contains(GameEventFlag.DISABLE_CRAFTING)) {
                e.setCancelled(true);
            }
        }
    }
}
