package com.tanner.minigames.kit.type;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BuilderKit extends Kit {
    private Minigames minigames;

    private int giveBlockDelay = 100;

    private int giveBlockRunnableID;

    public BuilderKit(Minigames minigames, UUID uuid) {
        super(minigames, TNTWarsKitType.BUILDER, uuid);
        this.minigames = minigames;
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.OAK_PLANKS, 10));
        giveBlockRunnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(minigames, () -> GivePlayerBlocks(player), giveBlockDelay, giveBlockDelay);
    }

    @Override
    public void onStop() {
        Bukkit.getScheduler().cancelTask(giveBlockRunnableID);
    }

    private void GivePlayerBlocks(Player player) {
        player.getInventory().addItem(new ItemStack(Material.OAK_PLANKS, 1));
    }
}
