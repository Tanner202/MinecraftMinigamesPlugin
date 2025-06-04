package com.tanner.minigames.instance.game.scrapshuffle;

import org.bukkit.Location;

public class Wall {

    private Location startLoc;
    private Location endLoc;

    public Wall(Location startLoc, Location endLoc) {
        this.startLoc = startLoc;
        this.endLoc = endLoc;
    }

    public Location getStart() {
        return startLoc;
    }

    public Location getEnd() {
        return endLoc;
    }
}
