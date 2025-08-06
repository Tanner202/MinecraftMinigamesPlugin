package com.tanner.minigames.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {

    public enum GuiType {
        ARENA,
        ARENA_MANAGE,
        TEAM_SPAWNS,
        GAMEMODE_SELECT
    }

    private final GuiType type;

    public CustomInventoryHolder(GuiType type) {
        this.type = type;
    }

    public GuiType getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
