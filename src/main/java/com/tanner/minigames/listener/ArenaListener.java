package com.tanner.minigames.listener;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class ArenaListener implements Listener {

    private Minigames minigames;

    public ArenaListener(Minigames minigames) {
        this.minigames = minigames;
    }

    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent e) {

        Bukkit.broadcastMessage("World Loaded!");
        Arena arena = minigames.getArena();
        arena.setCanJoin(true);
    }
}
