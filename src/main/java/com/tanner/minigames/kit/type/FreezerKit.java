package com.tanner.minigames.kit.type;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FreezerKit extends Kit {

    private Minigames minigames;

    private int reachDistance = 100;
    private int freezeDuration = 100;

    public FreezerKit(Minigames minigames, UUID uuid) {
        super(minigames, TNTWarsKitType.FREEZER, uuid);
        this.minigames = minigames;
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.ICE, 3));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getUniqueId() != getKitOwnerUUID()) return;

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType() == Material.ICE) {
            itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
            Vector playerFacing = player.getEyeLocation().getDirection();
            RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation().add(playerFacing.normalize()), playerFacing, reachDistance);

            if (rayTraceResult == null) return;
            Entity hitEntity = rayTraceResult.getHitEntity();

            Vector zero = new Vector(0, 0, 0);
            if (hitEntity != null && hitEntity.getType().equals(EntityType.PLAYER)) {
                Player hitPlayer = (Player) hitEntity;
                hitPlayer.sendMessage(ChatColor.AQUA + "You have been temporarily frozen!");
                int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(minigames, () -> hitEntity.setVelocity(zero), 0, 1);
                Bukkit.getScheduler().runTaskLater(minigames, () -> Bukkit.getScheduler().cancelTask(taskID), freezeDuration);
            }
        }
    }
}
