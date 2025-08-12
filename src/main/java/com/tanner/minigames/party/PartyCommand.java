package com.tanner.minigames.party;

import com.tanner.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private PartyManager partyManager;

    public PartyCommand(Minigames minigames) {
        partyManager = minigames.getPartyManager();
    }

    /*
    Commands:

    Leader:
    - /party invite [player]
    - /party remove [player]
    - /party disband

    Member:
    - /party join [player]
    - /party leave
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        Party party;
        if (args.length == 1) {
            switch (args[0]) {
                case "leave":
                    party = partyManager.getParty(player);
                    if (party == null) {
                        player.sendMessage(ChatColor.RED + "You cannot leave because you aren't in a party.");
                        return false;
                    }
                    player.sendMessage(ChatColor.RED + "You have left the party.");
                    party.removePartyMember(player.getUniqueId());
                    party.sendMessage(ChatColor.RED + player.getDisplayName() + " has left the party.");
                    break;
                case "disband":
                    party = partyManager.getParty(player);
                    if (party == null) {
                        player.sendMessage(ChatColor.RED + "You do not have a party to disband.");
                        return false;
                    }
                    player.sendMessage(ChatColor.GREEN + "Party disbanded.");
                    party.disbandParty();
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid Usage! These are the options:");
                    player.sendMessage(ChatColor.RED + "- /party invite <player>");
                    player.sendMessage(ChatColor.RED + "- /party remove <player>");
                    player.sendMessage(ChatColor.RED + "- /party join <player>");
                    player.sendMessage(ChatColor.RED + "- /party leave");
                    player.sendMessage(ChatColor.RED + "- /party disband");
            }
        } else if (args.length == 2) {
            String playerName = args[1];
            Player selectedPlayer = Bukkit.getPlayer(playerName);
            if (selectedPlayer == null) {
                player.sendMessage(ChatColor.RED + "The player " + playerName + " does not exist!");
                return false;
            }

            UUID senderUUID = player.getUniqueId();
            UUID selectedPlayerUUID = selectedPlayer.getUniqueId();
            switch (args[0]) {
                case "invite":
                    party = partyManager.getParty(player);

                    if (party == null) {
                        party = partyManager.createParty(player);
                    }

                    if (party.isPartyMember(senderUUID)) {
                        player.sendMessage(ChatColor.RED + "You cannot invite players because you aren't the leader.");
                        return false;
                    }

                    if (party.isPlayerInvited(selectedPlayerUUID)) {
                        player.sendMessage(ChatColor.RED + "You have already invited this player.");
                        return false;
                    }

                    party.invitePlayer(selectedPlayer);
                    break;
                case "remove":
                    party = partyManager.getParty(player);
                    if (party == null || party.isPartyLeader(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot remove this player because you do not have a party!");
                        return false;
                    }
                    player.sendMessage(ChatColor.RED + "You removed " + selectedPlayer.getDisplayName() + " from the party.");
                    selectedPlayer.sendMessage(ChatColor.RED + "You have been removed from the party.");
                    party.removePartyMember(selectedPlayerUUID);
                    party.sendMessage(ChatColor.RED + selectedPlayer.getDisplayName() + " has been removed from the party.");
                    break;
                case "join":
                    party = partyManager.getParty(selectedPlayer);
                    if (party == null) {
                        player.sendMessage(ChatColor.RED + "This player doesn't have a party.");
                        return false;
                    } else if (party.isInParty(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot join this party because you are already in one.");
                        return false;
                    } else if (!party.isPlayerInvited(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You cannot join this party because you were not invited!");
                        return false;
                    }
                    player.sendMessage(ChatColor.GREEN + "You have joined " + selectedPlayer.getDisplayName() + "'s party!");
                    party.addPartyMember(senderUUID);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid Usage! These are the options:");
                    player.sendMessage(ChatColor.RED + "- /party invite <player>");
                    player.sendMessage(ChatColor.RED + "- /party remove <player>");
                    player.sendMessage(ChatColor.RED + "- /party join <player>");
                    player.sendMessage(ChatColor.RED + "- /party leave");
                    player.sendMessage(ChatColor.RED + "- /party disband");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid Usage! These are the options:");
            player.sendMessage(ChatColor.RED + "- /party invite <player>");
            player.sendMessage(ChatColor.RED + "- /party remove <player>");
            player.sendMessage(ChatColor.RED + "- /party join <player>");
            player.sendMessage(ChatColor.RED + "- /party leave");
            player.sendMessage(ChatColor.RED + "- /party disband");
        }

        return false;
    }

}
