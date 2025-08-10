package com.tanner.minigames.command;

import com.tanner.minigames.gui.ArenaManageGUI;
import com.tanner.minigames.instance.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.KitUI;
import com.tanner.minigames.party.Party;
import com.tanner.minigames.party.PartyManager;
import com.tanner.minigames.team.TeamUI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArenaCommand implements CommandExecutor {

    private Minigames minigames;
    private ArenaManageGUI arenaManageGUI;
    private PartyManager partyManager;

    public ArenaCommand(Minigames minigames, ArenaManageGUI arenaManageGUI) {
        this.minigames = minigames;
        this.arenaManageGUI = arenaManageGUI;
        this.partyManager = minigames.getPartyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                player.sendMessage(ChatColor.GREEN + "These are the available arenas:");
                for (Arena arena : minigames.getArenaManager().getArenas()) {
                    player.sendMessage(ChatColor.GREEN + "- " + arena.getGameType().getDisplayName() + ChatColor.GREEN + " (#" + arena.getId() + ") (" + arena.getState().name() + ")");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("kit")) {
                Arena arena = minigames.getArenaManager().getArena(player);
                if (arena != null) {
                    if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                        new KitUI(player, arena.getKitTypes(), arena.getKit(player));
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

                Party party = partyManager.getParty(player);
                if (party != null && party.isPartyMember(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You cannot leave because you are in a party and aren't the party leader.");
                    return false;
                }

                if (arena != null) {
                    player.sendMessage(ChatColor.RED + "You left the arena.");
                    arena.removePlayer(player);
                    if (party != null && party.isPartyLeader(player)) {
                        for (UUID uuid : party.getPartyMembers()) {
                            arena.removePlayer(Bukkit.getPlayer(uuid));
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in an arena.");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                if (minigames.getArenaManager().getArena(player) != null) {
                    player.sendMessage(ChatColor.RED + "You are already playing in an arena");
                    return false;
                }

                Party party = partyManager.getParty(player);
                if (party != null && party.isPartyMember(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You cannot join because you are in a party and aren't the leader!");
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

                int playersJoining = 1;
                if (party != null && party.isPartyLeader(player)) {
                    playersJoining = party.getPartyMembers().size();
                }
                if (arena.getPlayers().size() + playersJoining > arena.getPlayerLimit()) {
                    player.sendMessage(ChatColor.RED + "This arena is too full to join.");
                    return false;
                }

                if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                    if (arena.canJoin()) {
                        player.sendMessage(ChatColor.GREEN + "You are now playing in arena " + id + ".");
                        arena.addPlayer(player);
                        if (party != null && party.isPartyLeader(player)) {
                            for (UUID uuid : party.getPartyMembers()) {
                                arena.addPlayer(Bukkit.getPlayer(uuid));
                            }
                        }
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
