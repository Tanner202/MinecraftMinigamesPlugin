package com.tanner.minigames.instance.game.dragonescape;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;

public class CustomPhase extends AbstractDragonPhaseInstance {

    private Vec3[] targets;
    private Vec3 currentTarget;
    private int currentTargetIndex;
    private final float distanceThreshold = 3;

    public CustomPhase(EnderDragon dragon, Vec3[] targets) {
        super(dragon);
        this.targets = targets;
        currentTargetIndex = 0;
        currentTarget = targets[currentTargetIndex];
    }

    @Override
    public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
        return EnderDragonPhase.CHARGING_PLAYER;
    }

    @Override
    public void doServerTick(ServerLevel var0) {
        moveDragonTowardsTarget();
        if (dragon.position().distanceTo(currentTarget) < distanceThreshold && currentTargetIndex + 1 < targets.length) {
            currentTargetIndex++;
            currentTarget = targets[currentTargetIndex];
        }
    }

    private void moveDragonTowardsTarget() {
        Vec3 current = dragon.position();
        Vec3 delta = currentTarget.subtract(current);

        // Calculate direction
        Vec3 direction = delta.normalize();

        // Movement speed (adjust this as needed)
        double speed = 0.2;

        // Apply motion
        dragon.setDeltaMovement(direction.scale(speed));

        // Make it move
        dragon.move(MoverType.SELF, dragon.getDeltaMovement());

        // Optional: adjust rotation to face direction
        float yaw = (float) (Math.toDegrees(Math.atan2(-delta.x, delta.z))) + 180;
        dragon.setYRot(yaw);
    }
}