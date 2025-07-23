package com.tanner.minigames.kit.kittype.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.ColorSwapKitType;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class SnowballerKit extends Kit {

    private int snowballTaskID;

    private long snowballCooldown = 8;
    private float knockbackMultiplier = 2;
    private int snowballAddAmount = 4;
    private int snowballSlot = 0;
    private int maxSnowballAmount = 16;

    public SnowballerKit(Minigames minigames, UUID uuid) {
        super(minigames, ColorSwapKitType.BALLER, uuid);
    }

    @Override
    public void onStart(Player player) {
        snowballTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(minigames, () -> {
            if ((!hasSnowballs(player)) || (hasSnowballs(player) && player.getInventory().getItem(snowballSlot).getAmount() < maxSnowballAmount)) {
                giveSnowballs(player);
            }
        },0, snowballCooldown * 20);
    }

    @Override
    public void onStop() {
        Bukkit.getScheduler().cancelTask(snowballTaskID);
    }

    private void giveSnowballs(Player player) {
        ItemStack snowballs = new ItemStack(Material.SNOWBALL, snowballAddAmount);
        if (hasSnowballs(player)
                && player.getInventory().getItem(snowballSlot).getAmount() + snowballs.getAmount() > maxSnowballAmount) {
            snowballs.setAmount(maxSnowballAmount - player.getInventory().getItem(snowballSlot).getAmount());
        }
        ItemMeta itemMeta = snowballs.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "Super Special Snowballs");
        snowballs.setItemMeta(itemMeta);

        if (player.getInventory().getItem(snowballSlot) != null && !player.getInventory().getItem(snowballSlot).getType().equals(Material.SNOWBALL)) {
            player.getInventory().setItem(snowballSlot, snowballs);
        } else {
            player.getInventory().addItem(snowballs);
        }
    }

    private boolean hasSnowballs(Player player) {
        return player.getInventory().getItem(snowballSlot) != null && player.getInventory().getItem(snowballSlot).getType().equals(Material.SNOWBALL);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Player hitPlayer = (Player) e.getHitEntity();
                e.getEntity().setVelocity(hitPlayer.getVelocity().multiply(knockbackMultiplier));
            }
        }
    }
}
