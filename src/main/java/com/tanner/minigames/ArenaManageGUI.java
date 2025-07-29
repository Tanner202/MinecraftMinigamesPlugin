package com.tanner.minigames;

import com.tanner.minigames.Utils.ItemBuilder;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.GameType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ArenaManageGUI implements Listener {

    private Minigames minigames;

    private HashMap<UUID, Arena> selectedArena = new HashMap<>();
    private List<UUID> playersSettingLobbySpawnpoints = new ArrayList<>();
    private List<UUID> playersSettingNPCSpawnpoints = new ArrayList<>();
    private List<UUID> playerGUIHistoryGroup = new ArrayList<>();

    public ArenaManageGUI(Minigames minigames) {
        this.minigames = minigames;
    }

    public void openArenaManagerGUI(Player player) {
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
    }

    public void openArenaGUI(Arena arena, Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.BOLD + arena.getGameType().getDisplayName());

        ItemStack gameTypeItem = ItemBuilder.createItem(Material.ENDER_EYE, ChatColor.BLUE + "Game Type: " + arena.getGameType());
        inv.addItem(gameTypeItem);

        Location spawn = arena.getSpawn();
        ItemStack lobbySpawnItem = ItemBuilder.createItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                ChatColor.GREEN + "Set Lobby Spawn",
                "Current Spawn: ",
                "World" + spawn.getWorld().getName(),
                "X: " + spawn.getX(),
                "Y: " + spawn.getY(),
                "Z: " + spawn.getZ(),
                "Yaw: " + spawn.getYaw(),
                "Pitch: " + spawn.getPitch());
        inv.addItem(lobbySpawnItem);

        List<String> lore = new ArrayList<>();
        if (arena.getNPC() != null) {
            Location npcSpawn = arena.getNPC().getLocation();
            lore = Arrays.asList("Current Location: ",
                    "World" + npcSpawn.getWorld().getName(),
                    "X: " + npcSpawn.getX(),
                    "Y: " + npcSpawn.getY(),
                    "Z: " + npcSpawn.getZ(),
                    "Yaw: " + spawn.getYaw(),
                    "Pitch: " + spawn.getPitch());
        }
        ItemStack npcSpawnItem = ItemBuilder.createItem(Material.VILLAGER_SPAWN_EGG, ChatColor.GREEN + "Set NPC Spawn", lore);
        inv.addItem(npcSpawnItem);

        ItemStack teamAmountItem = ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, ChatColor.GREEN + "Team Amount: " + arena.getAvailableTeams().length);
        inv.addItem(teamAmountItem);

        ItemStack maxPlayerAmountItem = ItemBuilder.createItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Max Player Amount: " + arena.getMaxPlayers());
        inv.addItem(maxPlayerAmountItem);

        boolean worldReloadEnabled = arena.worldReloadEnabled();
        ItemStack worldReloadItem = ItemBuilder.createItem(Material.END_PORTAL_FRAME, ChatColor.GREEN + "World Reload Enabled: " + (worldReloadEnabled ? ChatColor.GREEN : ChatColor.RED) + worldReloadEnabled);
        inv.addItem(worldReloadItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.GREEN.toString() + ChatColor.BOLD + "Arena Manager")) {

            if (e.getCurrentItem() == null) return;

            String arenaID = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Constants.ARENA_ID, PersistentDataType.STRING);
            Arena clickedArena = minigames.getArenaManager().getArena(Integer.valueOf(arenaID));
            selectedArena.put(player.getUniqueId(), clickedArena);
            openArenaGUI(clickedArena, player);
            e.setCancelled(true);
        } else {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {

                    switch (e.getRawSlot()) {
                        case 1:
                            playersSettingLobbySpawnpoints.add(player.getUniqueId());
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Set spawnpoint by standing at a location and typing 'confirm'");

                            break;
                        case 2:
                            playersSettingNPCSpawnpoints.add(player.getUniqueId());
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Set NPC spawnpoint by standing at a location and typing 'confirm'");
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    private void closeInventory(Player player, boolean withHistory) {
        if (withHistory) {
            playerGUIHistoryGroup.add(player.getUniqueId());
        }
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (playerGUIHistoryGroup.contains(player.getUniqueId())) {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {
                    Bukkit.getScheduler().runTaskLater(minigames, () -> openArenaManagerGUI(player), 1);
                }
            }
        }
    }

    @EventHandler
    public void playerChatEvent(PlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (playersSettingLobbySpawnpoints.contains(uuid)) {
            if (e.getMessage().equalsIgnoreCase("confirm")) {
                selectedArena.get(uuid).setLobbySpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Set Lobby Spawn Location!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else {
                player.sendMessage(ChatColor.RED + "Spawn Location Cancelled");
                player.playSound(player, Sound.ITEM_MACE_SMASH_GROUND, 1, 1);
            }
            openArenaGUI(selectedArena.get(uuid), player);
            e.setCancelled(true);
            playersSettingLobbySpawnpoints.remove(player.getUniqueId());
        } else if (playersSettingNPCSpawnpoints.contains(uuid)) {
            if (e.getMessage().equalsIgnoreCase("confirm")) {
                selectedArena.get(uuid).setNPCSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Set NPC Location!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else {
                player.sendMessage(ChatColor.RED + "Spawn Location Cancelled");
                player.playSound(player, Sound.ITEM_MACE_SMASH_GROUND, 1, 1);
            }
            openArenaGUI(selectedArena.get(uuid), player);
            e.setCancelled(true);
            playersSettingNPCSpawnpoints.remove(player.getUniqueId());
        }
    }
}
