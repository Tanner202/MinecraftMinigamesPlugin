package com.tanner.minigames;

import org.bukkit.NamespacedKey;

public class Constants {
    public static NamespacedKey TEAM_NAME;
    public static NamespacedKey KIT_NAME;

    public static void initializeConstants(Minigames minigames) {
        TEAM_NAME = new NamespacedKey(minigames, "TeamName");
        KIT_NAME = new NamespacedKey(minigames, "KitName");
    }
}
