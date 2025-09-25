package com.tanner.minigames.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Util {

    public static void sendPacket(Packet packet) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            connection.send(packet);
        }
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static Location getSnappedLocation(Location loc) {
        Location snappedLoc = new Location(
                loc.getWorld(),
                loc.getBlockX() + 0.5f,
                loc.getBlockY(),
                loc.getBlockZ() + 0.5f,
                Math.round(loc.getYaw() / 45) * 45,
                Math.round(loc.getPitch() / 45) * 45
        );
        return snappedLoc;
    }
}
