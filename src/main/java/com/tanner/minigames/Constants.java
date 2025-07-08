package com.tanner.minigames;

import org.bukkit.NamespacedKey;

public class Constants {
    public static NamespacedKey TEAM_NAME;
    public static NamespacedKey KIT_NAME;
    public static NamespacedKey TEAM_SELECTION;
    public static NamespacedKey KIT_SELECTION;
    public static NamespacedKey LEAVE_ITEM;
    public static int DEFAULT_REQUIRED_PLAYERS = 2;
    public static int DEFAULT_COUNTDOWN_TIME = 15;

    public static void initializeConstants(Minigames minigames) {
        TEAM_NAME = new NamespacedKey(minigames, "TeamName");
        KIT_NAME = new NamespacedKey(minigames, "KitName");
        TEAM_SELECTION = new NamespacedKey(minigames, "TeamSelection");
        KIT_SELECTION = new NamespacedKey(minigames, "KitSelection");
        LEAVE_ITEM = new NamespacedKey(minigames, "LeaveItem");
    }
}
