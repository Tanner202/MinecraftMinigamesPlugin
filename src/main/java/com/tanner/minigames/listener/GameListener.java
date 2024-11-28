package com.tanner.minigames.listener;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class GameListener implements Listener {

    private Minigames minigames;

    public GameListener(Minigames minigames) {
        this.minigames = minigames;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Arena arena = minigames.getArenaManager().getArena(player);

        if (arena != null && arena.getState().equals(GameState.LIVE)) {
            arena.getGame().addPoint(player);
        }
    }

}
