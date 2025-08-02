package com.tanner.minigames.command;

import com.tanner.minigames.ArenaManageGUI;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.KitUI;
import com.tanner.minigames.team.TeamUI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private Minigames minigames;
    private ArenaManageGUI arenaManageGUI;

    public ArenaCommand(Minigames minigames, ArenaManageGUI arenaManageGUI) {
        this.minigames = minigames;
        this.arenaManageGUI = arenaManageGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                player.sendMessage(ChatColor.GREEN + "These are the available arenas:");
                for (Arena arena : minigames.getArenaManager().getArenas()) {
                    player.sendMessage(ChatColor.GREEN + "- " + arena.getId() + " (" + arena.getState().name() + ")");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("kit")) {
                Arena arena = minigames.getArenaManager().getArena(player);
                if (arena != null) {
                    if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                        new KitUI(player, arena.getKitTypes());
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot use this right now.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in an arena.");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("team")) {
                Arena arena = minigames.getArenaManager().getArena(player);
                if (arena != null) {
                    if (arena.getState() != GameState.LIVE) {
                        new TeamUI(arena, player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot use this right now.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in an arena.");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
                Arena arena = minigames.getArenaManager().getArena(player);
                if (arena != null) {
                    player.sendMessage(ChatColor.RED + "You left the arena.");
                    arena.removePlayer(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in an arena.");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                if (minigames.getArenaManager().getArena(player) != null) {
                    player.sendMessage(ChatColor.RED + "You are already playing in an arena");
                    return false;
                }

                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "You specified an invalid arena ID.");
                    return false;
                }

                Arena arena = minigames.getArenaManager().getArena(id);

                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "This arena ID doesn't exist in the config.");
                    return false;
                }

                if (arena.getPlayers().size() >= arena.getPlayerLimit()) {
                    player.sendMessage(ChatColor.RED + "This arena is currently full.");
                    return false;
                }

                if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                    if (arena.canJoin()) {
                        player.sendMessage(ChatColor.GREEN + "You are now playing in arena " + id + ".");
                        arena.addPlayer(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot join this arena right now. Map is still loading.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot join this arena right now.");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("save")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "You specified an invalid arena ID.");
                    return false;
                }

                if (id >= 0 && id < minigames.getArenaManager().getArenas().size()) {
                    Arena arena = minigames.getArenaManager().getArena(id);

                    if (!arena.worldReloadEnabled()) {
                        return false;
                    }

                    if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                        arena.save();
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot save this arena right now.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You specified an invalid arena ID.");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("manage")) {
                arenaManageGUI.openArenaManagerGUI(player);
            } else {
                player.sendMessage(ChatColor.RED + "Invalid Usage! These are the options:");
                player.sendMessage(ChatColor.RED + "- /arena list");
                player.sendMessage(ChatColor.RED + "- /arena leave");
                player.sendMessage(ChatColor.RED + "- /arena join <id>");
                player.sendMessage(ChatColor.RED + "- /arena team");
                player.sendMessage(ChatColor.RED + "- /arena kit");
            }
        }

        return false;
    }
}
