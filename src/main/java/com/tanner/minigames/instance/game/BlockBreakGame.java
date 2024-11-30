package com.tanner.minigames.instance.game;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.UUID;

public class BlockBreakGame extends Game {

    private HashMap<UUID, Integer> points;

    public BlockBreakGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        points = new HashMap<>();
    }

    public void addPoint(Player player) {
        int playerPoints = points.get(player.getUniqueId()) + 1;
        if (playerPoints == 20) {
            arena.sendMessage(ChatColor.GOLD + player.getName() + " has Won! Thanks for Playing!");
            arena.reset(true);
            return;
        }

        player.sendMessage(ChatColor.GREEN + "+1 Point");
        points.replace(player.getUniqueId(), playerPoints);
    }

    @Override
    public void onStart() {
        for (UUID uuid : arena.getPlayers()) {
            points.put(uuid, 0);
        }
    }

    @Override
    public void onEnd() {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (arena.getPlayers().contains(e.getPlayer().getUniqueId()) && arena.getState().equals(GameState.LIVE)) {
            addPoint(e.getPlayer());
        }
    }
}
