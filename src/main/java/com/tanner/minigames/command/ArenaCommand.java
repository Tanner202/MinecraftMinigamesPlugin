package com.tanner.minigames.command;

import com.tanner.minigames.Constants;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.Utils.ItemBuilder;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ArenaCommand implements CommandExecutor, Listener {

    private Minigames minigames;

    private HashMap<UUID, Deque<Inventory>> guiHistory = new HashMap<>();
    private Set<UUID> suppressInventoryClose = new HashSet<>();

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

                suppressInventoryClose.add(player.getUniqueId());
                player.openInventory(inv);
                Deque<Inventory> inventoryHistory =  guiHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
                inventoryHistory.push(inv);

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

            ItemStack gameTypeItem = ItemBuilder.createItem(Material.ENDER_EYE, ChatColor.BLUE + "Game Type: " + gameType);
            inv.addItem(gameTypeItem);

            Location spawn = clickedArena.getSpawn();
            ItemStack lobbySpawnItem = ItemBuilder.createItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                    ChatColor.GREEN + "Set Lobby Spawn",
                    "Current Spawn: ",
                    "X: " + spawn.getX(),
                    "Y: " + spawn.getY(),
                    "Z: " + spawn.getZ());
            inv.addItem(lobbySpawnItem);

            List<String> lore = new ArrayList<>();
            if (clickedArena.getNPC() != null) {
                Location npcSpawn = clickedArena.getNPC().getLocation();
                lore = Arrays.asList("Current Location: ",
                        "X: " + npcSpawn.getX(),
                        "Y: " + npcSpawn.getY(),
                        "Z: " + npcSpawn.getZ());
            }
            ItemStack npcSpawnItem = ItemBuilder.createItem(Material.VILLAGER_SPAWN_EGG, ChatColor.GREEN + "Set NPC Spawn", lore);
            inv.addItem(npcSpawnItem);

            ItemStack teamAmountItem = ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, ChatColor.GREEN + "Team Amount: " + clickedArena.getAvailableTeams().length);
            inv.addItem(teamAmountItem);

            ItemStack maxPlayerAmountItem = ItemBuilder.createItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Max Player Amount: " + clickedArena.getMaxPlayers());
            inv.addItem(maxPlayerAmountItem);

            boolean worldReloadEnabled = clickedArena.worldReloadEnabled();
            ItemStack worldReloadItem = ItemBuilder.createItem(Material.END_PORTAL_FRAME, ChatColor.GREEN + "World Reload Enabled: " + (worldReloadEnabled ? ChatColor.GREEN : ChatColor.RED) + worldReloadEnabled);
            inv.addItem(worldReloadItem);

            suppressInventoryClose.add(player.getUniqueId());
            player.openInventory(inv);
            Deque<Inventory> inventoryHistory =  guiHistory.get(player.getUniqueId());
            inventoryHistory.push(inv);

            e.setCancelled(true);
        } else {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Deque<Inventory> inventoryHistory = guiHistory.get(player.getUniqueId());
        if (inventoryHistory.contains(e.getInventory()) && !suppressInventoryClose.contains(player.getUniqueId())) {
            inventoryHistory.pop();
            if (!inventoryHistory.isEmpty()) {
                Bukkit.getScheduler().runTaskLater(minigames,
                        () -> player.openInventory(inventoryHistory.getFirst()),
                        1);
            }
        }
        suppressInventoryClose.remove(player.getUniqueId());
    }
}
