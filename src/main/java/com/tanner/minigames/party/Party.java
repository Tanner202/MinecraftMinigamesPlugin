package com.tanner.minigames.party;

import com.tanner.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Party {

    private Minigames minigames;

    private Player partyLeader;
    private UUID leaderUUID;
    private List<UUID> partyMembers = new ArrayList<>();
    private HashMap<UUID, BukkitTask>  invitedPlayers = new HashMap<>();

    private int inviteRequestTime = 600;

    public Party(Minigames minigames, Player partyLeader) {
        this.minigames = minigames;
        this.partyLeader = partyLeader;
        this.leaderUUID = partyLeader.getUniqueId();
    }

    public void invitePlayer(Player invitee) {
        partyLeader.sendMessage(ChatColor.GREEN + "Invited " + invitee.getDisplayName() + " to your party!");
        invitee.sendMessage(ChatColor.GREEN + invitee.getDisplayName() + " has invited you to their party." +
                "\nType /party join " + partyLeader.getDisplayName() + " to join!");
        BukkitTask inviteExpireTask = Bukkit.getScheduler().runTaskLater(minigames, () -> inviteExpired(invitee), inviteRequestTime);
        invitedPlayers.put(invitee.getUniqueId(), inviteExpireTask);
    }

    public boolean isPlayerInvited(UUID playerUUID) {
        return invitedPlayers.containsKey(playerUUID);
    }

    private void inviteExpired(Player invitee) {
        partyLeader.sendMessage(ChatColor.RED + "Invite to " + invitee.getDisplayName() + " has expired.");
        invitee.sendMessage(ChatColor.RED + "Invite from " + partyLeader.getDisplayName() + " has expired.");
        invitedPlayers.remove(invitee.getUniqueId());
    }

    public void addPartyMember(UUID playerAdded) {
        sendMessage(ChatColor.GREEN + Bukkit.getPlayer(playerAdded).getDisplayName() + " has joined the party!");
        partyMembers.add(playerAdded);
        invitedPlayers.get(playerAdded).cancel();
        invitedPlayers.remove(playerAdded);
    }

    public void removePartyMember(UUID playerRemoved) {
        partyMembers.remove(playerRemoved);
        if (partyMembers.isEmpty()) {
            partyLeader.sendMessage(ChatColor.RED + "Party automatically disbanded because there are no other members.");
            disbandParty();
        }
    }

    public void disbandParty() {
        partyLeader = null;
        leaderUUID = null;
        partyMembers.clear();
        for (UUID uuid : partyMembers) {
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "The party you were in has been disbanded.");
        }
    }

    public boolean isPartyMember(UUID potentialPartyMember) {
       return partyMembers.contains(potentialPartyMember);
    }

    public List<UUID> getPartyMembers() {
        return partyMembers;
    }

    public boolean isPartyLeader(Player player) {
        return player.getUniqueId() == leaderUUID;
    }

    public boolean isInParty(Player player) {
        return partyLeader == player || partyMembers.contains(player.getUniqueId());
    }

    public void sendMessage(String message) {
        for (UUID uuid : getPartyMembers()) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
        partyLeader.sendMessage(message);
    }
}
