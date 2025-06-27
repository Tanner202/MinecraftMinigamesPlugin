package com.tanner.minigames.instance.game;

import com.mojang.authlib.GameProfile;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.Util;
import com.tanner.minigames.manager.ConfigManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VictoryCelebration {

    private Game game;
    private Minigames minigames;

    private int celebrationFireworkInterval = 20;
    private int crouchTaskInterval = 10;
    private int punchTaskInterval = 5;
    private BukkitTask fireworkTask;
    private BukkitTask punchTask;
    private BukkitTask crouchTask;

    public VictoryCelebration(Minigames minigames, Game game) {
        this.minigames = minigames;
        this.game = game;
        victoryCelebration();
    }

    protected void end() {
        fireworkTask.cancel();
        crouchTask.cancel();
        punchTask.cancel();
    }

    private void victoryCelebration() {
        for (UUID uuid : game.arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.teleport(ConfigManager.getSpawn("victory-podium.player-spawn"));
        }

        ServerPlayer playerNPC = addPlayerNPC();

        fireworkTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            for (Player player : game.winningPlayers) {
                player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
            }
        }, 0, celebrationFireworkInterval);

        SynchedEntityData data = playerNPC.getEntityData();
        EntityDataAccessor<Pose> POSE = new EntityDataAccessor<>(6, EntityDataSerializers.POSE);

        crouchTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            Pose pose = data.get(POSE);
            pose = (pose == Pose.CROUCHING) ? Pose.STANDING : Pose.CROUCHING;

            data.set(POSE, pose);
            Util.sendPacket(new ClientboundSetEntityDataPacket(playerNPC.getId(), data.packDirty()));
        }, 0, crouchTaskInterval);

        punchTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            Util.sendPacket(new ClientboundAnimatePacket(playerNPC, 0));
        }, 0, punchTaskInterval);
    }

    private ServerPlayer addPlayerNPC() {
        Player winningPlayer = game.winningPlayers.getFirst();
        CraftPlayer winningCraftPlayer = (CraftPlayer) winningPlayer;
        ServerPlayer winningServerPlayer = winningCraftPlayer.getHandle();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), winningPlayer.getDisplayName());

        ServerPlayer playerNPC = new ServerPlayer(winningServerPlayer.getServer(), winningServerPlayer.serverLevel(), gameProfile, ClientInformation.createDefault());
        Location npcPodiumSpawn = ConfigManager.getSpawn("victory-podium.npc-spawn");
        playerNPC.setPos(new Vec3(npcPodiumSpawn.getX(), npcPodiumSpawn.getY(), npcPodiumSpawn.getZ()));

        Set<ServerPlayerConnection> set = new HashSet<>();
        ServerEntity playerNPCServerEntity = new ServerEntity(playerNPC.serverLevel(), playerNPC, 0, false, packet -> {
        }, set);

        playerNPC.connection = new ServerGamePacketListenerImpl(playerNPC.getServer(), new Connection(PacketFlow.SERVERBOUND), playerNPC,
                CommonListenerCookie.createInitial(gameProfile, false));

        float yaw = npcPodiumSpawn.getYaw();
        float pitch = npcPodiumSpawn.getPitch();

        Util.sendPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, playerNPC));
        Util.sendPacket(new ClientboundAddEntityPacket(playerNPC, playerNPCServerEntity));
        Util.sendPacket(new ClientboundRotateHeadPacket(playerNPC, (byte) ((yaw % 360) * 256 / 360)));
        Util.sendPacket(new ClientboundMoveEntityPacket.Rot(playerNPC.getBukkitEntity().getEntityId(),
                (byte) ((yaw % 360) * 256 / 360),
                (byte) ((pitch % 360) * 256 / 360),
                true));
        return playerNPC;
    }
}
