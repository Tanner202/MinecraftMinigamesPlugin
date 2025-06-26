package com.tanner.minigames.instance.game;

import com.mojang.authlib.GameProfile;
import com.tanner.minigames.GameState;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.kit.Kit;
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
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public abstract class Game implements Listener {

    protected Minigames minigames;
    protected Arena arena;

    protected List<Player> winningPlayers;

    protected int arenaResetWaitTime = 200;
    private int celebrationFireworkInterval = 20;
    private BukkitTask celebrationTask;

    public Game(Minigames minigames, Arena arena) {
        this.arena = arena;
        this.minigames = minigames;
        this.winningPlayers = new ArrayList<>();
    }

    public void start() {
        arena.setState(GameState.LIVE);
        Bukkit.getPluginManager().registerEvents(this, minigames);
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().clear();
            Kit kit = arena.getKits().get(uuid);
            if (kit != null) {
                kit.onStart(Bukkit.getPlayer(uuid));
            }
        }
        onStart();
    }

    public void end(boolean gameComplete) {
        unregisterEvents();
        onEnd();
        if (gameComplete) {
            victoryCelebration();
            Bukkit.getScheduler().runTaskLater(minigames, () -> {
                celebrationTask.cancel();
                arena.reset(true);
            }, arenaResetWaitTime);
        } else {
            arena.reset(true);
        }
    }

    protected void victoryCelebration() {

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.teleport(ConfigManager.getSpawn("victory-podium.player-spawn"));
        }

        for (Player player : winningPlayers) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();

            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getDisplayName());

            ServerPlayer playerNPC = new ServerPlayer(serverPlayer.getServer(), serverPlayer.serverLevel(), gameProfile, ClientInformation.createDefault());
            Location npcPodiumSpawn = ConfigManager.getSpawn("victory-podium.npc-spawn");
            playerNPC.setPos(new Vec3(npcPodiumSpawn.getX(), npcPodiumSpawn.getY(), npcPodiumSpawn.getZ()));

            Set<ServerPlayerConnection> set = new HashSet<>();
            ServerEntity playerNPCServerEntity = new ServerEntity(playerNPC.serverLevel(), playerNPC, 0, false, packet -> {
            }, set);

            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            SynchedEntityData data = playerNPC.getEntityData();
            byte bitmask = (byte) (0x01 | 0x04 | 0x08 | 0x010 | 0x20 | 0x40);
            data.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), bitmask);

            playerNPC.connection = new ServerGamePacketListenerImpl(playerNPC.getServer(), new Connection(PacketFlow.SERVERBOUND), playerNPC,
                    CommonListenerCookie.createInitial(gameProfile, false));

            connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, playerNPC));
            connection.send(new ClientboundAddEntityPacket(playerNPC, playerNPCServerEntity));

            float yaw = npcPodiumSpawn.getYaw();
            float pitch = npcPodiumSpawn.getPitch();
            connection.send(new ClientboundRotateHeadPacket(playerNPC, (byte) ((yaw % 360) * 256 / 360)));
            connection.send(new ClientboundMoveEntityPacket.Rot(playerNPC.getBukkitEntity().getEntityId(),
                    (byte) ((yaw % 360) * 256 / 360),
                    (byte) ((pitch % 360) * 256 / 360),
                    true));
        }

        celebrationTask = Bukkit.getScheduler().runTaskTimer(minigames, () -> {
            for (Player player : winningPlayers) {
                player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
        }}, 0, celebrationFireworkInterval);
    }

    public abstract void onStart();
    public abstract void onEnd();
    public abstract void onPlayerRemoved(Player player);

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }
}
