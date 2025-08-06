package com.tanner.minigames.gui;

public enum GuiItem {

    GAME_TYPE(4),
    LOBBY_SPAWN(12),
    TEAM_SPAWN(13),
    NPC_SPAWN(14),
    TEAM_SIZE(21),
    PLAYER_LIMIT(22),
    WORLD_RELOAD(23),
    DELETE_ARENA(18),
    BACK(26);

    private int slot;

    GuiItem(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
