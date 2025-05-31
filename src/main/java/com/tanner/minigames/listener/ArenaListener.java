package com.tanner.minigames.listener;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ArenaListener implements Listener {

    private Minigames minigames;

    public ArenaListener(Minigames minigames) {
        this.minigames = minigames;
    }

    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent e) {

        Bukkit.broadcastMessage("World Loaded!");
        Arena arena = minigames.getArenaManager().getArena(e.getWorld());
        if (arena != null) {
            Bukkit.broadcastMessage("Found arena with world: " + e.getWorld().getName());
            arena.setCanJoin(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Arena arena = minigames.getArenaManager().getArena(minigames.getArenaManager().getArena(e.getRightClicked().getUniqueId()));
        if (e.getHand() == EquipmentSlot.HAND && e.getRightClicked().getType().equals(EntityType.VILLAGER) && arena != null) {
            Bukkit.dispatchCommand(e.getPlayer(), "arena join " + arena.getId());
        }
    }
}
