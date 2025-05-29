package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColorSwapGame extends Game {

    private Grid grid;
    private List<UUID> remainingPlayers;

    private int gridSize = 25;
    private int cellSize = 5;

    public ColorSwapGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        grid = new Grid(minigames, arena, arena.getSpawn(), gridSize, cellSize);
        remainingPlayers = new ArrayList<>();
    }

    @Override
    public void onStart() {
        remainingPlayers.addAll(arena.getPlayers());
        grid.start();
    }

    @Override
    public void onEnd() {
        grid.Stop();
        remainingPlayers.clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (arena.getPlayers().contains(player.getUniqueId())) {
            minigames.getServer().getScheduler().scheduleSyncDelayedTask(minigames, () -> {
                if (player.isDead()) {
                    player.spigot().respawn();
                    player.teleport(arena.getSpawn());
                    player.setGameMode(GameMode.SPECTATOR);
                }
                remainingPlayers.remove(player.getUniqueId());
                if (remainingPlayers.size() == 1) {
                    Player winningPlayer = Bukkit.getPlayer(remainingPlayers.get(0));
                    arena.sendMessage(ChatColor.GOLD + winningPlayer.getDisplayName() + " has Won! Thanks for Playing!");
                    winningPlayers.add(winningPlayer);
                    end();
                }
            });
        }
    }
}
