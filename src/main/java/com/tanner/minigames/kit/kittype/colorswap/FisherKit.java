package com.tanner.minigames.kit.kittype.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.ColorSwapKitType;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FisherKit extends Kit {

    public FisherKit(Minigames minigames, UUID uuid) {
        super(minigames, ColorSwapKitType.FISHER, uuid);
    }

    @Override
    public void onStart(Player player) {
        ItemStack fishingRod = new ItemStack(Material.FISHING_ROD);
        ItemMeta itemMeta = fishingRod.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "Player Fisher");
        itemMeta.setUnbreakable(true);
        fishingRod.setItemMeta(itemMeta);
        player.getInventory().addItem(fishingRod);
    }
}
