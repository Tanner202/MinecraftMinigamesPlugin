package com.tanner.minigames.team;

import com.tanner.minigames.util.Constants;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TeamUI {
    public TeamUI(Arena arena, Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLUE + "Team Selection");

        for (Team team : arena.getAvailableTeams()) {
            ItemStack itemStack = ItemBuilder.createItem(team.getMaterial(), team.getDisplay() + " " + ChatColor.GRAY + "(" + arena.getTeamCount(team) + " players)",
                    Constants.TEAM_NAME, PersistentDataType.STRING, team.name());

            gui.addItem(itemStack);
        }

        player.openInventory(gui);
    }
}
