package com.tanner.minigames.listener;

import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectListener implements Listener {

    private Minigames minigames;

    public ConnectListener(Minigames minigames) {
        this.minigames = minigames;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {

        if (minigames.getArena().getState() == GameState.LIVE) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "You cannot join this game right now because it's currently in progress");
        } else if (!minigames.getArena().canJoin()) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "You cannot join this game right now because it's currently resetting");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        minigames.getArena().addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        minigames.getArena().removePlayer(e.getPlayer());
    }
}
