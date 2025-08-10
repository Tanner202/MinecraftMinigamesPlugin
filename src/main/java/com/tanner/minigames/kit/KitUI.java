package com.tanner.minigames.kit;

import com.tanner.minigames.util.Constants;
import com.tanner.minigames.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class KitUI {

    public KitUI(Player player, KitType[] availableKitTypes, KitType selectedKitType) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Kit Selection");

        for (KitType type : availableKitTypes) {
            ItemStack itemStack;
            if (type == selectedKitType) {
                itemStack = ItemBuilder.createItem(type.getMaterial(), type.getDisplay(),
                        Constants.KIT_NAME, PersistentDataType.STRING, type.getName(), type.getDescription(),
                        ChatColor.GREEN.toString() + ChatColor.BOLD + "SELECTED");
            } else {
                itemStack = ItemBuilder.createItem(type.getMaterial(), type.getDisplay(),
                        Constants.KIT_NAME, PersistentDataType.STRING, type.getName(), type.getDescription());
            }

            gui.addItem(itemStack);
        }


        player.openInventory(gui);
    }
}
