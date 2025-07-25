package com.tanner.minigames.command;

import com.tanner.minigames.Constants;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.GameType;
import com.tanner.minigames.kit.KitUI;
import com.tanner.minigames.team.TeamUI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ArenaCommand implements CommandExecutor, Listener {

    private Minigames minigames;

    public ArenaCommand(Minigames minigames) {
        this.minigames = minigames;
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

                if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
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
                Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN.toString() + ChatColor.BOLD + "Arena Manager");

                for (Arena arena : minigames.getArenaManager().getArenas()) {
                    GameType gameType = arena.getGameType();
                    ItemStack arenaItem = new ItemStack(gameType.getDisplayIcon());
                    ItemMeta arenaMeta = arenaItem.getItemMeta();
                    arenaMeta.getPersistentDataContainer().set(Constants.ARENA_ID, PersistentDataType.STRING,  String.valueOf(arena.getId()));
                    arenaMeta.setItemName(gameType.getDisplayName());
                    arenaItem.setItemMeta(arenaMeta);
                    inv.addItem(arenaItem);
                }

                player.openInventory(inv);
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.GREEN.toString() + ChatColor.BOLD + "Arena Manager")) {

            if (e.getCurrentItem() == null) return;

            String arenaID = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Constants.ARENA_ID, PersistentDataType.STRING);
            Arena clickedArena = minigames.getArenaManager().getArena(Integer.valueOf(arenaID));
            GameType gameType = clickedArena.getGameType();
            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.BOLD + gameType.getDisplayName());

            ItemStack gameTypeItem = new ItemStack(Material.ENDER_EYE);
            ItemMeta gameTypeMeta = gameTypeItem.getItemMeta();
            gameTypeMeta.setItemName(ChatColor.BLUE + "Game Type: " + gameType);
            gameTypeItem.setItemMeta(gameTypeMeta);
            inv.addItem(gameTypeItem);

            Location spawn = clickedArena.getSpawn();
            ItemStack lobbySpawnItem = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            ItemMeta lobbySpawnMeta = lobbySpawnItem.getItemMeta();
            lobbySpawnMeta.setItemName(ChatColor.GREEN + "Set Lobby Spawn");
            lobbySpawnMeta.setLore(Arrays.asList("Current Spawn: ",
                    "X: " + spawn.getX(),
                    "Y: " + spawn.getY(),
                    "Z: " + spawn.getZ()));
            lobbySpawnItem.setItemMeta(lobbySpawnMeta);
            inv.addItem(lobbySpawnItem);

            ItemStack npcSpawnItem = new ItemStack(Material.VILLAGER_SPAWN_EGG);
            ItemMeta npcSpawnMeta = npcSpawnItem.getItemMeta();
            npcSpawnMeta.setItemName(ChatColor.GREEN + "Set NPC Spawn");
            if (clickedArena.getNPC() != null) {
                Location npcSpawn = clickedArena.getNPC().getLocation();
                npcSpawnMeta.setLore(Arrays.asList("Current Location: ",
                        "X: " + npcSpawn.getX(),
                        "Y: " + npcSpawn.getY(),
                        "Z: " + npcSpawn.getZ()));
            }
            npcSpawnItem.setItemMeta(npcSpawnMeta);
            inv.addItem(npcSpawnItem);

            ItemStack teamAmountItem = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemMeta teamAmountMeta = teamAmountItem.getItemMeta();
            teamAmountMeta.setItemName(ChatColor.GREEN + "Team Amount: " + clickedArena.getAvailableTeams().length);
            teamAmountItem.setItemMeta(teamAmountMeta);
            inv.addItem(teamAmountItem);

            ItemStack maxPlayerAmountItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta maxPlayerAmountMeta = maxPlayerAmountItem.getItemMeta();
            maxPlayerAmountMeta.setItemName(ChatColor.GREEN + "Max Player Amount: " + clickedArena.getMaxPlayers());
            maxPlayerAmountItem.setItemMeta(maxPlayerAmountMeta);
            inv.addItem(maxPlayerAmountItem);

            boolean worldReloadEnabled = clickedArena.worldReloadEnabled();
            ItemStack worldReloadItem = new ItemStack(Material.END_PORTAL_FRAME);
            ItemMeta worldReloadMeta = worldReloadItem.getItemMeta();
            worldReloadMeta.setItemName(ChatColor.GREEN + "World Reload Enabled: " + (worldReloadEnabled ? ChatColor.GREEN : ChatColor.RED) + worldReloadEnabled);
            worldReloadItem.setItemMeta(worldReloadMeta);
            inv.addItem(worldReloadItem);

            player.openInventory(inv);

            e.setCancelled(true);
        } else {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
