package com.tanner.minigames.instance.game.scrapshuffle;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetCrateCommand implements CommandExecutor, Listener {

    private HashMap<UUID, Team> playersPlacingCrates;
    private YamlConfiguration crateLocFile;
    private File file;

    public SetCrateCommand(Minigames minigames) {
        playersPlacingCrates = new HashMap<>();
        file = minigames.getFileManager().getFile("scrapyard_skirmish/crate_locations.yml");
        if (file != null) {
            crateLocFile = YamlConfiguration.loadConfiguration(file);
        }
        Bukkit.getPluginManager().registerEvents(this, minigames);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                for (Team team : Team.values()) {
                    if (args[0].equals(ChatColor.stripColor(team.getDisplay().toLowerCase()))) {
                        UUID uuid = player.getUniqueId();
                        if (playersPlacingCrates.containsKey(uuid)) {
                            player.sendMessage(ChatColor.RED + "Placing crates toggled off");
                            playersPlacingCrates.remove(uuid);
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Placing crates toggled on for " + team.getDisplay() + " team!");
                            playersPlacingCrates.put(uuid, team);
                        }
                        return false;
                    }
                }
            }

            player.sendMessage(ChatColor.RED + "Incorrect Usage: \n" +
                    "/setcrate [red/blue/green/yellow]");
        }
        return false;
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (playersPlacingCrates.containsKey(uuid) && e.getBlockPlaced().getType().equals(Material.CHEST)) {
            Location loc = e.getBlock().getLocation();
            player.sendMessage(ChatColor.GREEN + "Set crate at (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");

            String teamName = ChatColor.stripColor(playersPlacingCrates.get(uuid).getDisplay()).toLowerCase();
            List<String> keys = new ArrayList<>(crateLocFile.getConfigurationSection(teamName).getKeys(false));
            try {
                int crateID = Integer.parseInt(keys.getLast()) + 1;
                String prefix = teamName + "." + crateID;
                crateLocFile.set(prefix + ".world", loc.getWorld().getName());
                crateLocFile.set(prefix + ".x", loc.getX());
                crateLocFile.set(prefix + ".y", loc.getY());
                crateLocFile.set(prefix + ".z", loc.getZ());

                crateLocFile.save(file);
            } catch (NumberFormatException exc) {
                throw new NumberFormatException();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (playersPlacingCrates.containsKey(uuid) && e.getBlock().getType().equals(Material.CHEST)) {
            Location blockLocation = e.getBlock().getLocation();

            String teamName = ChatColor.stripColor(playersPlacingCrates.get(uuid).getDisplay()).toLowerCase();
            for (String crateID : crateLocFile.getConfigurationSection(teamName).getKeys(false)) {
                String prefix = ChatColor.stripColor(teamName).toLowerCase() + "." + crateID;
                Location crateLocation = new Location(Bukkit.getWorld(crateLocFile.getString(prefix + ".world")),
                        crateLocFile.getDouble(prefix + ".x"),
                        crateLocFile.getDouble(prefix + ".y"),
                        crateLocFile.getDouble(prefix + ".z"));
                if (crateLocation.equals(blockLocation)) {
                    crateLocFile.set(prefix, null);
                    try {
                        crateLocFile.save(file);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    player.sendMessage(ChatColor.RED + "Removed crate at (" + crateLocation.getX() + ", " + crateLocation.getY() + ", " + crateLocation.getZ() + ")");
                }
            }
        }
    }
}
