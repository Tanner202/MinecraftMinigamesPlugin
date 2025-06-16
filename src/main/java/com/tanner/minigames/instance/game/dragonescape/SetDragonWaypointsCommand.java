package com.tanner.minigames.instance.game.dragonescape;

import com.tanner.minigames.Minigames;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetDragonWaypointsCommand implements CommandExecutor, Listener {

    private List<UUID> activeWaypointEditors = new ArrayList<>();
    private YamlConfiguration ymlFile;
    private File file;
    private List<ArmorStand> waypoints = new ArrayList<>();
    private World world;

    public SetDragonWaypointsCommand(Minigames minigames) {
        file = minigames.getFileManager().getFile(Paths.get("dragon_escape/dragon_locations.yml"));
        ymlFile = YamlConfiguration.loadConfiguration(file);
        world = Bukkit.getWorld(ymlFile.getString("dragon-spawn.world"));

        if (ymlFile.getConfigurationSection("target-locations") == null) return;

        for (String key : ymlFile.getConfigurationSection("target-locations").getKeys(false)) {
            String prefix = "target-locations.";
            Location loc = new Location(world,
                    ymlFile.getDouble(prefix + key + ".x"),
                    ymlFile.getDouble(prefix + key + ".y"),
                    ymlFile.getDouble(prefix + key + ".z"));
            boolean foundArmorStand = false;
            for (Entity entity : world.getNearbyEntities(loc, 2, 2, 2)) {
                if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                    waypoints.add((ArmorStand) entity);
                    foundArmorStand = true;
                }
            }
            if (!foundArmorStand) {
                spawnArmorStand(loc);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!activeWaypointEditors.contains(player.getUniqueId())) {
                activeWaypointEditors.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Enabled Dragon Waypoint Editing");
                setWaypointsVisible(true);
            } else {
                activeWaypointEditors.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Disabled Dragon Waypoint Editing");
                setWaypointsVisible(false);
            }
        }
        return false;
    }

    private void setWaypointsVisible(boolean isVisible) {
        for (ArmorStand armorStand : waypoints) {
            armorStand.setInvisible(!isVisible);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws IOException {
        Player player = e.getPlayer();
        if (activeWaypointEditors.contains(player.getUniqueId()) && e.getClickedBlock() != null && e.getItem() != null
                && e.getItem().getType().equals(Material.ARMOR_STAND)) {
            e.setCancelled(true);
            int newKey;
            if (!ymlFile.getConfigurationSection("target-locations").getKeys(false).isEmpty()) {
                String[] keys = ymlFile.getConfigurationSection("target-locations").getKeys(false).toArray(new String[0]);
                newKey = Integer.parseInt(keys[keys.length - 1]) + 1;
            } else {
                newKey = 0;
            }
            ArmorStand armorStand = spawnArmorStand(e.getClickedBlock().getLocation());
            String prefix = "target-locations.";
            Location loc = armorStand.getLocation();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            ymlFile.set(prefix + newKey + ".x", x);
            ymlFile.set(prefix + newKey + ".y", y);
            ymlFile.set(prefix + newKey + ".z", z);
            ymlFile.save(file);
            player.sendMessage(ChatColor.GREEN + "Set waypoint at (" + x + ", " + y + ", " + z + ")");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    private ArmorStand spawnArmorStand(Location loc) {
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.ARMOR_STAND);
        if (activeWaypointEditors.isEmpty()) {
            armorStand.setInvisible(true);
        }
        armorStand.setInvulnerable(true);
        waypoints.add(armorStand);
        return armorStand;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) throws IOException {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        Location loc = entity.getLocation();
        if (activeWaypointEditors.contains(player.getUniqueId()) && entity.getType().equals(EntityType.ARMOR_STAND) &&
                waypoints.contains((ArmorStand) entity)) {
            waypoints.remove(entity);
            entity.remove();

            for (String key : ymlFile.getConfigurationSection("target-locations").getKeys(false)) {
                String prefix = "target-locations.";
                Location fileLoc = new Location(world,
                        ymlFile.getDouble(prefix + key + ".x"),
                        ymlFile.getDouble(prefix + key + ".y"),
                        ymlFile.getDouble(prefix + key + ".z"));
                if (fileLoc.equals(loc)) {
                    ymlFile.set(prefix + key, null);
                    ymlFile.save(file);
                }
            }

            player.sendMessage(ChatColor.RED + "Removed waypoint at (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        }
    }
}
