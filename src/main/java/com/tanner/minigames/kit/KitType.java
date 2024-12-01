package com.tanner.minigames.kit;

import com.tanner.minigames.Minigames;
import org.bukkit.Material;

import java.util.UUID;

public interface KitType {
    String getDisplay();
    Material getMaterial();
    String getDescription();
    String getName();
    Kit createKit(Minigames minigames, UUID uuid);
}
