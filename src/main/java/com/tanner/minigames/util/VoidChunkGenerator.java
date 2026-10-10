package com.tanner.minigames.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

// A ChunkGenerator that doesn't override any generation methods creates completely empty chunks (a void world)
public class VoidChunkGenerator extends ChunkGenerator {

    // Fixed spawn so the server doesn't try to search for solid ground in an empty world
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0.5, 100, 0.5);
    }
}
