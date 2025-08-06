package com.tanner.minigames.listener;

import com.tanner.minigames.util.Constants;
import com.tanner.minigames.instance.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.KitType;
import com.tanner.minigames.kit.KitUI;
import com.tanner.minigames.team.Team;
import com.tanner.minigames.team.TeamUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GameLobbyListener implements Listener {

    private Minigames minigames;

    public GameLobbyListener(Minigames minigames) {
        this.minigames = minigames;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena == null) return;
        if (!arena.getState().equals(GameState.RECRUITING) && !arena.getState().equals(GameState.COUNTDOWN)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() != null && e.getView().getTitle().contains("Team Selection")) {
            ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
            if (itemMeta == null) return;

            Team team = null;
            for (Team t : Team.values()) {
                if (t.name().equals(itemMeta.getPersistentDataContainer().get(Constants.TEAM_NAME, PersistentDataType.STRING))) {
                    team = t;
                }
            }
            if (team == null) return;

            boolean isTeamTooLarge = false;
            for (Team _team : arena.getTeams()) {
                if (arena.getTeamCount(team) >= arena.getTeamCount(_team) && team != _team) {
                    isTeamTooLarge = true;
                }
            }

            if (arena.getTeam(player) == team) {
                player.sendMessage(ChatColor.RED + "You are already on this team.");
            } else if (isTeamTooLarge) {
                player.sendMessage(ChatColor.RED + "This team is too large to join.");
            } else {
                player.sendMessage(ChatColor.AQUA + "You are now on " + team.getDisplay() + ChatColor.AQUA + " team.");
                arena.setTeam(player, team);
            }

            player.closeInventory();
        } else if (e.getCurrentItem() != null && e.getView().getTitle().contains("Kit Selection")) {
            ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
            if (itemMeta == null) return;

            KitType kitType = null;
            for (KitType type : arena.getKitTypes()) {
                if (type.getName().equals(itemMeta.getPersistentDataContainer().get(Constants.KIT_NAME, PersistentDataType.STRING))) {
                    kitType = type;
                }
            }
            if (kitType == null) return;


            KitType activated = arena.getKit(player);
            if (activated != null && activated == kitType) {
                player.sendMessage(ChatColor.RED + "You already have this kit selected.");
            } else {
                player.sendMessage(ChatColor.GREEN + "You have selected the " + kitType.getDisplay() + ChatColor.GREEN + " kit!");
                arena.setKit(player.getUniqueId(), kitType);
            }

            player.closeInventory();
        }
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Arena arena = minigames.getArenaManager().getArena(player);

        if (arena == null) return;
        if (!arena.getState().equals(GameState.RECRUITING) && !arena.getState().equals(GameState.COUNTDOWN)) return;

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ItemMeta itemMeta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
            if (itemMeta == null) return;

            if (itemMeta.getPersistentDataContainer().has(Constants.TEAM_SELECTION)) {
                e.setCancelled(true);
                player.updateInventory();
                new TeamUI(arena, player);
            } else if (itemMeta.getPersistentDataContainer().has(Constants.KIT_SELECTION)) {
                e.setCancelled(true);
                new KitUI(player, arena.getKitTypes());
            } else if (itemMeta.getPersistentDataContainer().has(Constants.LEAVE_ITEM)) {
                e.setCancelled(true);
                Bukkit.dispatchCommand(player, "arena leave");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Arena arena = minigames.getArenaManager().getArena(e.getPlayer());
        if (arena == null) return;
        if (!arena.getState().equals(GameState.RECRUITING) && !arena.getState().equals(GameState.COUNTDOWN)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena == null) return;
        if (!arena.getState().equals(GameState.RECRUITING) && !arena.getState().equals(GameState.COUNTDOWN)) return;

        player.sendMessage(ChatColor.RED + "You cannot break blocks at this time.");
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        Arena arena = minigames.getArenaManager().getArena(player);
        if (arena == null) return;
        if (!arena.getState().equals(GameState.RECRUITING) && !arena.getState().equals(GameState.COUNTDOWN)) return;

        player.sendMessage(ChatColor.RED + "You cannot place blocks at this time.");
        e.setCancelled(true);
    }
}
