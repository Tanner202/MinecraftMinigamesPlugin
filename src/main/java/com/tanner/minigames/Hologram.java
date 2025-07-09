package com.tanner.minigames;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    final private float spacing = 0.3f;
    private List<ArmorStand> armorStands = new ArrayList<>();

    public Hologram(Location loc, String[] lines) {
        Location hologramLoc = loc.add(0 , (lines.length - 1) * spacing, 0);
        for (String line : lines) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(line);
            hologramLoc.subtract(0, spacing, 0);
            armorStands.add(stand);
        }
    }

    public void removeHologram() {
        for (ArmorStand stand : armorStands) {
            stand.remove();
        }
    }
}
