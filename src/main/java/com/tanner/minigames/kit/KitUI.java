package com.tanner.minigames.kit;

import com.tanner.minigames.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class KitUI {

    public KitUI(Player player, KitType[] availableKitTypes) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Kit Selection");

        for (KitType type : availableKitTypes) {
            ItemStack itemStack = new ItemStack(type.getMaterial());
            ItemMeta isMeta = itemStack.getItemMeta();
            isMeta.setDisplayName(type.getDisplay());
            isMeta.setLore(Arrays.asList(type.getDescription()));
            isMeta.getPersistentDataContainer().set(Constants.KIT_NAME, PersistentDataType.STRING, type.getName());
            itemStack.setItemMeta(isMeta);

            gui.addItem(itemStack);
        }


        player.openInventory(gui);
    }
}
