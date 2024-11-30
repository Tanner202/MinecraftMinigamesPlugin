package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PVPGame extends Game {

    public PVPGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
    }

    public void playerWins(Player player) {
        arena.sendMessage(ChatColor.GOLD + player.getName() + " has Won! Thanks for Playing!");
        arena.reset(true);
    }

    @Override
    public void onStart() {
        for (UUID uuid : arena.getPlayers()) {
            Bukkit.getPlayer(uuid).getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        }
    }

    @Override
    public void onEnd() {}

    @EventHandler
    public void onBlockBreak(PlayerDeathEvent e) {
        if (arena.getPlayers().contains(e.getEntity()) && arena.getPlayers().contains(e.getEntity().getKiller()) && arena.getState() == GameState.LIVE) {
            playerWins(e.getEntity().getKiller());
        }
    }
}
