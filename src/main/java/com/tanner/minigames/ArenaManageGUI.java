package com.tanner.minigames;

import com.tanner.minigames.Utils.ItemBuilder;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.GameType;
import com.tanner.minigames.team.Team;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ArenaManageGUI implements Listener {

    private Minigames minigames;

    private HashMap<UUID, Arena> selectedArena = new HashMap<>();
    private HashMap<UUID, String> playersSettingSpawnpoints = new HashMap<>();
    private HashMap<UUID, String> playersChatInputting = new HashMap<>();
    private List<UUID> guiHistoryWhitelistGroup = new ArrayList<>();

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

        ItemStack addArenaItem = ItemBuilder.createItem(Material.NETHER_STAR, ChatColor.GREEN + "Add Arena", "Click to add arena!");
        ItemMeta addArenaMeta = addArenaItem.getItemMeta();
        addArenaMeta.getPersistentDataContainer().set(Constants.ADD_ARENA_ITEM, PersistentDataType.BOOLEAN, true);
        addArenaItem.setItemMeta(addArenaMeta);
        inv.addItem(addArenaItem);

        fillEmptySlots(inv, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(inv);
    }

    public void openArenaGUI(Arena arena, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BOLD + arena.getGameType().getDisplayName());

        ItemStack gameTypeItem = ItemBuilder.createItem(Material.ENDER_EYE, ChatColor.BLUE + "Game Type: " + arena.getGameType());
        inv.setItem(4, gameTypeItem);

        Location spawn = arena.getSpawn();
        ItemStack lobbySpawnItem = ItemBuilder.createItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                ChatColor.GREEN + "Set Lobby Spawn",
                "Current Spawn: ",
                "World: " + spawn.getWorld().getName(),
                "X: " + spawn.getX(),
                "Y: " + spawn.getY(),
                "Z: " + spawn.getZ(),
                "Yaw: " + spawn.getYaw(),
                "Pitch: " + spawn.getPitch());
        inv.setItem(12, lobbySpawnItem);

        ItemStack teamSpawnItem = ItemBuilder.createItem(Material.RED_BED, ChatColor.GREEN + "Team Spawns");
        inv.setItem(13, teamSpawnItem);

        List<String> lore = new ArrayList<>();
        if (arena.getNPC() != null) {
            Location npcSpawn = arena.getNPC().getLocation();
            lore = Arrays.asList("Current Location: ",
                    "World: " + npcSpawn.getWorld().getName(),
                    "X: " + npcSpawn.getX(),
                    "Y: " + npcSpawn.getY(),
                    "Z: " + npcSpawn.getZ(),
                    "Yaw: " + spawn.getYaw(),
                    "Pitch: " + spawn.getPitch());
        }
        ItemStack npcSpawnItem = ItemBuilder.createItem(Material.VILLAGER_SPAWN_EGG, ChatColor.GREEN + "Set NPC Spawn", lore);
        inv.setItem(14, npcSpawnItem);

        ItemStack teamAmountItem = ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, ChatColor.GREEN + "Team Size: " + arena.getTeamSize());
        inv.setItem(21, teamAmountItem);

        ItemStack playerLimit = ItemBuilder.createItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Player Limit: " + arena.getPlayerLimit());
        inv.setItem(22, playerLimit);

        boolean worldReloadEnabled = arena.worldReloadEnabled();
        ItemStack worldReloadItem = ItemBuilder.createItem(Material.END_PORTAL_FRAME, ChatColor.GREEN + "World Reload Enabled: " + (worldReloadEnabled ? ChatColor.GREEN : ChatColor.RED) + worldReloadEnabled);
        inv.setItem(23, worldReloadItem);

        ItemStack deleteArenaItem = ItemBuilder.createItem(Material.LAVA_BUCKET, ChatColor.RED + "Delete Arena");
        inv.setItem(18, deleteArenaItem);

        ItemStack backButtonItem = ItemBuilder.createItem(Material.BARRIER, ChatColor.RED + "Exit");
        inv.setItem(26, backButtonItem);

        fillEmptySlots(inv, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(inv);
    }

    public void openCreateArenaGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GREEN.toString() + ChatColor.BOLD + "Select Gamemode: ");

        for (GameType gameType : GameType.values()) {
            inv.addItem(ItemBuilder.createItem(gameType.getDisplayIcon(), gameType.getDisplayName()));
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.GREEN.toString() + ChatColor.BOLD + "Arena Manager")) {

            if (e.getCurrentItem() == null) return;

            PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
            if (container.has(Constants.ARENA_ID)) {
                String arenaID = container.get(Constants.ARENA_ID, PersistentDataType.STRING);
                Arena clickedArena = minigames.getArenaManager().getArena(Integer.valueOf(arenaID));
                selectedArena.put(player.getUniqueId(), clickedArena);
                openArenaGUI(clickedArena, player);
            } else if (container.has(Constants.ADD_ARENA_ITEM)) {
                openCreateArenaGUI(player);
            }
            e.setCancelled(true);
        } else if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD.toString() + ChatColor.GREEN + "Team Spawns")) {
            Arena arena = selectedArena.get(player.getUniqueId());
            if (e.getRawSlot() == 0) {
                playersSettingSpawnpoints.put(player.getUniqueId(), "all_team");
                closeInventory(player, false);
                player.sendMessage(ChatColor.GREEN + "Set spawnpoint by standing in location and typing 'confirm'. Type anything else to cancel.");
            }
            int count = 1;
            for (Team team : arena.getAvailableTeams()) {
                if (count == e.getRawSlot()) {
                    playersSettingSpawnpoints.put(player.getUniqueId(), team.toString());
                    closeInventory(player, false);
                    player.sendMessage(ChatColor.GREEN + "Set spawnpoint by standing in location and typing 'confirm'. Type anything else to cancel.");
                }
                count++;
            }
        } else if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.GREEN.toString() + ChatColor.BOLD + "Select Gamemode: ")) {
            GameType selectedGameType = Arrays.asList(GameType.values()).get(e.getRawSlot());
            closeInventory(player, false);
            GameSettings gameSettings = new GameSettings(selectedGameType,
                    player.getLocation(), null, 1,
                    8, false);
            Arena arena = minigames.getArenaManager().addArena(gameSettings);
            selectedArena.put(player.getUniqueId(), arena);
            playersSettingSpawnpoints.put(player.getUniqueId(), "lobby_setup");
            player.sendMessage(ChatColor.GREEN + "Set Lobby Spawn by standing in a location and typing 'confirm'. Type anything else to cancel arena creation.");
        } else {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {

                    Arena arena = selectedArena.get(player.getUniqueId());
                    switch (e.getRawSlot()) {
                        case 12:
                            playersSettingSpawnpoints.put(player.getUniqueId(), "lobby");
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Set spawnpoint by standing at a location and typing 'confirm'");

                            break;
                        case 13:
                            Inventory inv = Bukkit.createInventory(null, 18, ChatColor.BOLD.toString() + ChatColor.GREEN + "Team Spawns");

                            ItemStack allTeamSpawn = ItemBuilder.createItem(Material.NETHER_STAR, ChatColor.DARK_PURPLE + "All Team Spawn");
                            inv.addItem(allTeamSpawn);
                            for (Team team : arena.getAvailableTeams()) {
                                Location spawn = arena.getTeamSpawn(team);
                                List<String> lore;
                                if (spawn != null) {
                                    lore = Arrays.asList("World: " + spawn.getWorld().getName(),
                                            "X: " + spawn.getX(),
                                            "Y: " + spawn.getY(),
                                            "Z: " + spawn.getZ(),
                                            "Yaw: " + spawn.getYaw(),
                                            "Pitch: " + spawn.getPitch());
                                } else {
                                    lore = Arrays.asList(ChatColor.RED + "Spawn not set");
                                }
                                ItemStack spawnItem = ItemBuilder.createItem(team.getMaterial(), team.getDisplay(),
                                        lore);
                                inv.addItem(spawnItem);
                            }
                            closeInventory(player, false);
                            player.openInventory(inv);
                            break;
                        case 14:
                            playersSettingSpawnpoints.put(player.getUniqueId(), "npc");
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Set NPC spawnpoint by standing at a location and typing 'confirm'");
                            break;
                        case 21:
                            playersChatInputting.put(player.getUniqueId(), "team_size");
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Send a number in chat to set team size: ");
                            break;
                        case 22:
                            playersChatInputting.put(player.getUniqueId(), "player_limit");
                            closeInventory(player, false);
                            player.sendMessage(ChatColor.GREEN + "Send a number in chat to set player limit: ");
                            break;
                        case 23:
                            arena.setWorldReloadEnabled(!arena.worldReloadEnabled());
                            closeInventory(player, false);
                            openArenaGUI(arena, player);
                            break;
                        case 18:
                            playersChatInputting.put(player.getUniqueId(), "delete_arena");
                            player.sendMessage(ChatColor.RED + "Type 'DELETE ARENA' to confirm arena deletion. Type anything else to cancel.");
                            closeInventory(player, false);
                            break;
                        case 26:
                            closeInventory(player, true);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    private void fillEmptySlots(Inventory inv, Material material) {
        ItemStack fillItem = ItemBuilder.createItem(material, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, fillItem);
            }
        }
    }

    private void closeInventory(Player player, boolean withHistory) {
        if (!withHistory) {
            guiHistoryWhitelistGroup.add(player.getUniqueId());
        }
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (guiHistoryWhitelistGroup.contains(player.getUniqueId())) {
            guiHistoryWhitelistGroup.remove(player.getUniqueId());
        } else {
            for (GameType gameType : GameType.values()) {
                if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD + gameType.getDisplayName())) {
                    Bukkit.getScheduler().runTaskLater(minigames, () -> openArenaManagerGUI(player), 1);
                }
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.GREEN.toString() + ChatColor.BOLD + "Select Gamemode: ")) {
                Bukkit.getScheduler().runTaskLater(minigames, () -> openArenaManagerGUI(player), 1);
            } else if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.BOLD.toString() + ChatColor.GREEN + "Team Spawns")) {
                Bukkit.getScheduler().runTaskLater(minigames, () -> openArenaGUI(selectedArena.get(player.getUniqueId()), player), 1);
            }
        }
    }

    @EventHandler
    public void playerChatEvent(PlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (playersSettingSpawnpoints.containsKey(uuid)) {
            Arena arena = selectedArena.get(uuid);
            if (e.getMessage().equalsIgnoreCase("confirm")) {
                String spawnType = playersSettingSpawnpoints.get(uuid);
                Location spawn = player.getLocation();
                switch (spawnType) {
                    case "lobby":
                        arena.setLobbySpawn(spawn);
                        break;
                    case "lobby_setup":
                        minigames.getArenaManager().saveArena(arena);
                        arena.setLobbySpawn(spawn);
                        openArenaGUI(arena, player);
                        break;
                    case "npc":
                        arena.setNPCSpawn(spawn);
                        break;
                    case "all_team":
                        arena.setTeamSpawn(Team.ALL, spawn);
                        break;
                    default:
                        for (Team team : arena.getAvailableTeams()) {
                            if (spawnType.equalsIgnoreCase(team.toString())) {
                                arena.setTeamSpawn(team, spawn);
                            }
                        }
                        break;
                }
                player.sendMessage(ChatColor.GREEN + "Set Spawn Location!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else {
                player.sendMessage(ChatColor.RED + "Spawn Location Cancelled");
                player.playSound(player, Sound.ITEM_MACE_SMASH_GROUND, 1, 1);
            }
            openArenaGUI(selectedArena.get(uuid), player);
            e.setCancelled(true);
            playersSettingSpawnpoints.remove(uuid);
        } else if (playersChatInputting.containsKey(uuid)) {
            Arena arena = selectedArena.get(uuid);
            if (playersChatInputting.get(uuid).equalsIgnoreCase("team_size")) {
                try {
                    int teamSize = Integer.parseInt(e.getMessage());
                    arena.setTeamSize(teamSize);
                    player.sendMessage(ChatColor.GREEN + "Set team size to " + teamSize + ".");
                } catch (NumberFormatException exc) {
                    player.sendMessage(ChatColor.RED + "You didn't enter a number for team size.");
                }
                openArenaGUI(arena, player);
            } else if (playersChatInputting.get(uuid).equalsIgnoreCase("player_limit")) {
                try {
                    int playerLimit = Integer.parseInt(e.getMessage());
                    arena.setPlayerLimit(playerLimit);
                    player.sendMessage(ChatColor.GREEN + "Set player limit to " + playerLimit + ".");
                } catch (NumberFormatException exc) {
                    player.sendMessage(ChatColor.RED + "You didn't enter a number for player limit.");
                }
                openArenaGUI(arena, player);
            } else if (playersChatInputting.get(uuid).equalsIgnoreCase("delete_arena")) {
                if (e.getMessage().equals("DELETE ARENA")) {
                    player.sendMessage(ChatColor.GREEN + "Arena successfully deleted.");
                    minigames.getArenaManager().deleteArena(arena);
                } else {
                    player.sendMessage(ChatColor.RED + "Arena deletion cancelled.");
                    openArenaGUI(arena, player);
                }
            }
            playersChatInputting.remove(uuid);
            e.setCancelled(true);
        }
    }
}
