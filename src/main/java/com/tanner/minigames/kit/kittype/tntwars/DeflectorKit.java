package com.tanner.minigames.kit.kittype.tntwars;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;

public class DeflectorKit extends Kit {

    private Minigames minigames;

    private float tntLaunchPower = 1.5f;
    private float tntHeight = 0.5f;
    private int fuseTime = 45;

    private int reachDistance = 3;

    public DeflectorKit(Minigames minigames, UUID uuid) {
        super(minigames, TNTWarsKitType.DEFLECTOR, uuid);
        this.minigames = minigames;
    }

    @Override
    public void onStart(Player player) { }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent e)
    {
        Player player = e.getPlayer();
        if (player.getUniqueId() != getKitOwnerUUID()) return;
        if (!e.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) return;

        Vector playerFacing = player.getEyeLocation().getDirection();
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation().add(playerFacing.normalize()), playerFacing, reachDistance);

        if (rayTraceResult == null) return;
        Entity hitEntity = rayTraceResult.getHitEntity();

        if (hitEntity != null && hitEntity.getType().equals(EntityType.PRIMED_TNT)) {
            Vector heightVector = new Vector(0, tntHeight, 0);
            hitEntity.setVelocity(playerFacing.multiply(tntLaunchPower).add(heightVector));
            TNTPrimed primedTNT = (TNTPrimed) hitEntity;
            primedTNT.setFuseTicks(fuseTime);
        }
    }
}
