package com.tanner.minigames.kit.kittype.colorswap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.ColorSwapKitType;
import com.tanner.minigames.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DoubleJumperKit extends Kit {

    private Player kitOwner;
    private int doubleJumpCooldown = 3;
    private Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(doubleJumpCooldown, TimeUnit.SECONDS).build();
    private int remainingUses = 3;
    private float playerDoubleJumpPower = 1f;
    private float forwardPower = 1f;

    public DoubleJumperKit(Minigames minigames, UUID uuid) {
        super(minigames, ColorSwapKitType.DOUBLE_JUMPER, uuid);
    }


    @Override
    public void onStart(Player player) {
        kitOwner = player;
        kitOwner.setAllowFlight(true);
        kitOwner.setFlying(false);
    }

    @Override
    public void onStop() {
        if (!kitOwner.getGameMode().equals(GameMode.CREATIVE) && !kitOwner.getGameMode().equals(GameMode.SPECTATOR)) {
            kitOwner.setFlying(false);
            kitOwner.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();

        if (!cooldown.asMap().containsKey(player.getUniqueId()) && remainingUses != 0) {
            cooldown.put(player.getUniqueId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + doubleJumpCooldown);
        } else {
            long timeRemaining = cooldown.asMap().get(player.getUniqueId()) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            player.sendMessage(ChatColor.RED + "You must wait " + timeRemaining + " second(s) before using double jump!");
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().equals(kitOwner)) {
            e.setCancelled(true);
            if (remainingUses > 0) {
                Vector playerDirection = player.getLocation().getDirection();
                Vector doubleJumpVector = new Vector(playerDirection.getX() * forwardPower, playerDoubleJumpPower,
                        playerDirection.getZ() * forwardPower);
                player.setVelocity(doubleJumpVector);

                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
                remainingUses--;
                kitOwner.sendMessage(ChatColor.GREEN + "Double Jump used! " + remainingUses + " use" + (remainingUses == 1 ? "" : "s") + " remaining!");
            } else {
                kitOwner.sendMessage(ChatColor.RED + "You are out of usages!");
            }
        }
    }
}
