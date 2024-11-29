package com.tanner.minigames.kit;

import com.tanner.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class Kit implements Listener {
    private KitType type;
    private UUID kitOwnerUUID;

    public Kit(Minigames minigames, KitType type, UUID uuid) {
        this.type = type;
        this.kitOwnerUUID = uuid;

        Bukkit.getPluginManager().registerEvents(this, minigames);
    }

    public UUID getKitOwnerUUID() { return kitOwnerUUID; }
    public KitType getType() { return type; }

    public abstract void onStart(Player player);
    public void onStop() { }

    public void remove() {
        HandlerList.unregisterAll(this);
        onStop();
    }
}
