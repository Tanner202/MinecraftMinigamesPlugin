package com.tanner.minigames.kit.kittype.tntwars;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tanner.minigames.Minigames;
import com.tanner.minigames.kit.Kit;
import com.tanner.minigames.kit.TNTWarsKitType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SneakyKit extends Kit {

    private int sneakDuration = 120;
    private long sneakCooldownDuration = 15;
    private Cache<UUID, Long> sneakCooldown = CacheBuilder.newBuilder().expireAfterWrite(sneakCooldownDuration, TimeUnit.SECONDS).build();
    private NamespacedKey sneakKey;

    public SneakyKit(Minigames minigames, UUID uuid) {
        super(minigames, TNTWarsKitType.SNEAKY, uuid);
        sneakKey = new NamespacedKey(minigames, "sneak_ability");
    }

    @Override
    public void onStart(Player player) {
        ItemStack sneakItem = new ItemStack(Material.INK_SAC, 1);
        ItemMeta sneakItemMeta = sneakItem.getItemMeta();
        sneakItemMeta.getPersistentDataContainer().set(sneakKey, PersistentDataType.BOOLEAN, true);
        sneakItemMeta.setDisplayName(ChatColor.DARK_PURPLE + "Sneak");
        String lore = ChatColor.GRAY + "Activate Sneak Ability";
        sneakItemMeta.setLore(Arrays.asList(lore));
        sneakItem.setItemMeta(sneakItemMeta);

        player.getInventory().addItem(sneakItem);
    }

    @Override
    public void onStop() {

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getUniqueId() != getKitOwnerUUID()) return;

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = heldItem.getItemMeta();
        if (itemMeta == null) return;

        if (itemMeta.getPersistentDataContainer().get(sneakKey, PersistentDataType.BOOLEAN) != null) {
            if (!sneakCooldown.asMap().containsKey(player.getUniqueId())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, sneakDuration, 1, false, false));
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 10);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
                sneakCooldown.asMap().put(player.getUniqueId(), System.currentTimeMillis() + sneakCooldownDuration * 1000);
            } else {
                long distance = sneakCooldown.asMap().get(player.getUniqueId()) - System.currentTimeMillis();
                long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(distance);
                player.sendMessage(ChatColor.RED + "Your sneak ability is on cooldown for " + remainingSeconds +
                        " more second" + (remainingSeconds == 1 ? "" : "s"));
            }
        }
    }
}
