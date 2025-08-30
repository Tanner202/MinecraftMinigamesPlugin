package com.tanner.minigames.instance.game.dragonescape;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

import java.lang.reflect.Field;

public class CustomEnderDragon extends EnderDragon {

    public CustomEnderDragon(Level world, Location startingLoc, Vec3[] targets) throws NoSuchFieldException, IllegalAccessException {
        super(EntityType.ENDER_DRAGON, world);
        EnderDragonPhaseManager phaseManager = getPhaseManager();

        Field phasesField = EnderDragonPhaseManager.class.getDeclaredField("phases");
        phasesField.setAccessible(true);

        DragonPhaseInstance[] phases = (DragonPhaseInstance[]) phasesField.get(phaseManager);
        phases[EnderDragonPhase.CHARGING_PLAYER.getId()] = new CustomPhase(this, targets);
        getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);

        world.addFreshEntity(this);
        setPosRaw(startingLoc.getX(), startingLoc.getY(), startingLoc.getZ());
    }
}
