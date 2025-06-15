package com.tanner.minigames.instance.game.dragonescape;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;

import java.nio.file.Paths;

public class DragonEscapeGame extends Game {

    private YamlConfiguration file;
    private Location dragonSpawnLocation;
    private CustomEnderDragon customDragon;

    public DragonEscapeGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
        file = YamlConfiguration.loadConfiguration(minigames.getFileManager().getFile(Paths.get("dragon_escape/dragon_locations.yml")));
        dragonSpawnLocation = getDragonSpawn(file);
    }

    private Location getDragonSpawn(YamlConfiguration file) {
        String prefix = "dragon-spawn";
        return new Location(Bukkit.getWorld(file.getString(prefix + ".world")),
                file.getDouble(prefix + ".x"),
                file.getDouble(prefix + ".y"),
                file.getDouble(prefix + ".z"),
                (float) file.getDouble(prefix + ".yaw"),
                (float) file.getDouble(prefix + ".pitch"));
    }

    @Override
    public void onStart() {
        Vec3[] targets = getTargetLocations();
        Bukkit.broadcastMessage(targets.toString());
        try {
            customDragon = new CustomEnderDragon(((CraftWorld) arena.getWorld()).getHandle().getLevel(), dragonSpawnLocation, targets);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Vec3[] getTargetLocations() {
        int targetAmount = file.getConfigurationSection("target-locations").getKeys(false).size();
        Vec3[] targets = new Vec3[targetAmount];
        int currentTargetIndex = 0;
        String prefix = "target-locations.";
        for (String key : file.getConfigurationSection("target-locations").getKeys(false)) {
            double x = file.getDouble(prefix + key + ".x");
            double y = file.getDouble(prefix + key + ".y");
            double z = file.getDouble(prefix + key + ".z");
            Vec3 target = new Vec3(x, y, z);
            targets[currentTargetIndex] = target;
            currentTargetIndex++;
        }
        return targets;
    }

    @Override
    public void onEnd() {

    }
}
