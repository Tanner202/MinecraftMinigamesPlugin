package com.tanner.minigames.instance.game.dragonescape;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.util.ItemBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetDragonWaypointsCommand implements CommandExecutor, Listener {

    private Minigames minigames;

    private List<UUID> activeWaypointEditors = new ArrayList<>();
    private YamlConfiguration ymlFile;
    private File file;
    private List<ArmorStand> waypoints = new ArrayList<>();
    private World world;
    private int selectedWaypointIndex = -1;

    BukkitTask selectedWaypointMessage;

    public SetDragonWaypointsCommand(Minigames minigames) {
        this.minigames = minigames;
        file = minigames.getFileManager().getFile(Paths.get("dragon_escape/dragon_locations.yml"));
        ymlFile = YamlConfiguration.loadConfiguration(file);
        String worldName = ymlFile.getString("dragon-spawn.world");
        world = Bukkit.getWorld(worldName);

        if (world == null) {
            Bukkit.getLogger().warning("World " + worldName + " not found, using fallback.");
            world = Bukkit.getWorld("world");
        }
        if (ymlFile.getConfigurationSection("target-locations") == null) return;

        for (String key : ymlFile.getConfigurationSection("target-locations").getKeys(false)) {
            String prefix = "target-locations.";
            Location loc = new Location(world,
                    ymlFile.getDouble(prefix + key + ".x"),
                    ymlFile.getDouble(prefix + key + ".y"),
                    ymlFile.getDouble(prefix + key + ".z"));
            boolean foundArmorStand = false;
            for (Entity entity : world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                    waypoints.add((ArmorStand) entity);
                    foundArmorStand = true;
                }
            }
            if (!foundArmorStand) {
                addWaypoint(loc);
            }
        }

        selectedWaypointIndex = waypoints.size() - 1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!activeWaypointEditors.contains(player.getUniqueId())) {
                activeWaypointEditors.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Enabled Dragon Waypoint Editing");
                giveWaypointItems(player);
                setWaypointsVisible(true);
                selectedWaypointMessage = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
                    if (selectedWaypointIndex >= 0) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a§lSelected Waypoint: " + selectedWaypointIndex));
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a§lSelected Waypoint: None"));
                    }
                }, 0, 40);
            } else {
                activeWaypointEditors.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Disabled Dragon Waypoint Editing");
                player.getInventory().clear();
                setWaypointsVisible(false);
                selectedWaypointMessage.cancel();
                try {
                    saveWaypointsToYAML();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    private void setWaypointsVisible(boolean isVisible) {
        for (ArmorStand armorStand : waypoints) {
            armorStand.setInvisible(!isVisible);
        }
    }

    private void giveWaypointItems(Player player) {
        Inventory inv = player.getInventory();

        ItemStack addWaypointItem = ItemBuilder.createItem(Material.ARMOR_STAND,
                ChatColor.GREEN + "Waypoint Creator",
                "Right click a block to add a waypoint after the current waypoint");

        ItemStack removeWaypointItem = ItemBuilder.createItem(Material.IRON_AXE,
                ChatColor.RED + "Waypoint Remover",
                "Right click a waypoint to remove it!");

        ItemStack waypointSelectorItem = ItemBuilder.createItem(Material.STICK,
                ChatColor.AQUA + "Waypoint Selector",
                "Right click a waypoint to select it!");

        ItemStack waypointTeleporterItem = ItemBuilder.createItem(Material.COMPASS,
                ChatColor.DARK_PURPLE + "Waypoint Teleporter (Right Click)");

        ItemStack saveItem = ItemBuilder.createItem(Material.PAPER,
                ChatColor.GREEN + "Save");

        inv.setItem(0, addWaypointItem);
        inv.setItem(1, removeWaypointItem);
        inv.setItem(2, waypointSelectorItem);
        inv.setItem(4, waypointTeleporterItem);
        inv.setItem(8, saveItem);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (activeWaypointEditors.contains(player.getUniqueId())) {
            if (e.getClickedBlock() != null && e.getItem() != null
                    && e.getItem().getType().equals(Material.ARMOR_STAND)) {
                e.setCancelled(true);
                ArmorStand armorStand = addWaypoint(e.getClickedBlock().getLocation());
                Location loc = armorStand.getLocation();
                player.sendMessage(ChatColor.GREEN + "Set waypoint at (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                selectedWaypointIndex++;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a§lSelected Waypoint: " + selectedWaypointIndex));
            } else if (e.getItem().getType().equals(Material.COMPASS)) {
                Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Waypoint Teleporter");
                for (int i = 0; i < waypoints.size(); i++) {
                    Location loc = waypoints.get(i).getLocation();
                    ItemStack stand = ItemBuilder.createItem(Material.ARMOR_STAND,
                            ChatColor.GREEN + "Waypoint " + i,
                            "X: " + loc.getX(), "Y: " + loc.getY(), "Z: " + loc.getZ());
                    gui.setItem(i, stand);
                }
                player.openInventory(gui);
            } else if (player.getInventory().getItemInMainHand().getType().equals(Material.PAPER)) {
                try {
                    saveWaypointsToYAML();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
                player.sendMessage(ChatColor.GREEN + "Saved Waypoints to YAML!");
            }
        }
    }

    private void saveWaypointsToYAML() throws IOException {
        ConfigurationSection waypointsSection = ymlFile.getConfigurationSection("target-locations");
        if (waypointsSection != null) {
            for (String key : waypointsSection.getKeys(false)) {
                waypointsSection.set(key, null);
            }
        } else {
            ymlFile.createSection("target-locations"); // ensure it exists if not already
        }

        int waypointIndex = 0;
        for (ArmorStand stand : waypoints) {
            String prefix = "target-locations.";
            Location loc = stand.getLocation();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            ymlFile.set(prefix + waypointIndex + ".x", x);
            ymlFile.set(prefix + waypointIndex + ".y", y);
            ymlFile.set(prefix + waypointIndex + ".z", z);
            waypointIndex++;
        }
        ymlFile.save(file);
    }

    private ArmorStand addWaypoint(Location loc) {
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.ARMOR_STAND);
        if (activeWaypointEditors.isEmpty()) {
            armorStand.setInvisible(true);
        }
        armorStand.setInvulnerable(true);
        if (selectedWaypointIndex + 1 > waypoints.size() - 1) {
            waypoints.add(armorStand);
        } else {
            waypoints.add(selectedWaypointIndex + 1, armorStand);
        }
        return armorStand;
    }

    private void removeWaypoint(Entity stand, Location loc) {
        waypoints.remove(stand);
        stand.remove();

        for (String key : ymlFile.getConfigurationSection("target-locations").getKeys(false)) {
            String prefix = "target-locations.";
            Location fileLoc = new Location(world,
                    ymlFile.getDouble(prefix + key + ".x"),
                    ymlFile.getDouble(prefix + key + ".y"),
                    ymlFile.getDouble(prefix + key + ".z"));
            if (fileLoc.equals(loc)) {
                ymlFile.set(prefix + key, null);
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Waypoint Teleporter")
        && e.getCurrentItem() != null) {
            Player player = (Player) e.getWhoClicked();
            player.teleport(waypoints.get(e.getRawSlot()).getLocation());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        Location loc = entity.getLocation();
        if (activeWaypointEditors.contains(player.getUniqueId()) && entity.getType().equals(EntityType.ARMOR_STAND) &&
                waypoints.contains((ArmorStand) entity)) {

            if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE)) {
                removeWaypoint(entity, loc);

                player.sendMessage(ChatColor.RED + "Removed waypoint at (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                selectedWaypointIndex--;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a§lSelected Waypoint: " + selectedWaypointIndex));
            } else if ((player.getInventory().getItemInMainHand().getType().equals(Material.STICK))) {
                player.sendMessage(ChatColor.GREEN + "Selected waypoint");
                for (ArmorStand stand : waypoints) {
                    if (stand.getLocation().equals(entity.getLocation())) {
                        selectedWaypointIndex = waypoints.indexOf(stand);
                    }
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a§lSelected Waypoint: " + selectedWaypointIndex));
            }
        }
    }
}
