package com.tanner.minigames.kit.kittype.tntwars;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class LastChanceKit extends Kit {
    private float upwardPower = 2f;

    public LastChanceKit(Minigames minigames, UUID uuid) {
        super(minigames, TNTWarsKitType.LAST_CHANCE, uuid);
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 1));
    }

    @Override
    public void onStop() {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getUniqueId() != getKitOwnerUUID()) return;

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType() == Material.FIREWORK_ROCKET &&
                (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            e.setCancelled(true);
            itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
            player.setVelocity(player.getVelocity().setY(upwardPower));
        }
    }
}
