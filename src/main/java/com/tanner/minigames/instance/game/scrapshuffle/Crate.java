package com.tanner.minigames.instance.game.scrapshuffle;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class Crate {

    private Chest chest;
    private CrateData crateData;
    private Random random;

    public Crate(CrateData crateData, Chest chest) {
        this.chest = chest;
        this.crateData = crateData;
        random = new Random();
        setCrateContents();
    }

    private void setCrateContents() {

        int itemAmount = random.nextInt(crateData.getMinItems(), crateData.getMaxItems() + 1);
        for (int i = 0; i < itemAmount; i++) {

            Material item;
            // Settings rarity of items based off of amount
            if (i < crateData.getCommonItemAmount()) {
                item = getRandomItem(CrateRarity.COMMON);
            } else if (i == crateData.getUncommonItemAmount()) {
                item = getRandomItem(CrateRarity.UNCOMMON);
            } else {
                item = getRandomItem(CrateRarity.RARE);
            }

            int randomSlot = random.nextInt(0, chest.getBlockInventory().getSize());
            while (chest.getBlockInventory().getItem(randomSlot) != null) {
                randomSlot = random.nextInt(0, chest.getBlockInventory().getSize());
            }
            chest.getBlockInventory().setItem(randomSlot, new ItemStack(item, 1));
        }
    }

    private Material getRandomItem(CrateRarity rarity) {
        List<Material> items = crateData.getCrateItems().get(rarity);
        int randomIndex = random.nextInt(0, items.size());
        return items.get(randomIndex);
    }

}
