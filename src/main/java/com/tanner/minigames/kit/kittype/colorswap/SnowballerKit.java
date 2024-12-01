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

    public SnowballerKit(Minigames minigames, UUID uuid) {
        super(minigames, ColorSwapKitType.BALLER, uuid);
    }

    @Override
    public void onStart(Player player) {
        snowballTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(minigames, () -> {
            giveSnowballs(player);
        },0, snowballCooldown * 20);
    }

    @Override
    public void onStop() {
        Bukkit.getScheduler().cancelTask(snowballTaskID);
    }

    private void giveSnowballs(Player player) {
        ItemStack snowballs = new ItemStack(Material.SNOWBALL, 4);
        ItemMeta itemMeta = snowballs.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "Super Special Snowballs");
        snowballs.setItemMeta(itemMeta);
        player.getInventory().addItem(snowballs);
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
