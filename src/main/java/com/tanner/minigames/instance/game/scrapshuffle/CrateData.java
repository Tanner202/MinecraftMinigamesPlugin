package com.tanner.minigames.instance.game.scrapshuffle;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrateData {

    private HashMap<CrateRarity, List<Material>> crateItems;
    private int minItems;
    private int maxItems;
    private int commonItemAmount;
    private int uncommonItemAmount;
    private int rareItemAmount;
    private int crateAmount;

    public CrateData(YamlConfiguration crateFile) {
        crateItems = new HashMap<>();
        minItems = crateFile.getInt("min-items");
        maxItems = crateFile.getInt("max-items");
        crateAmount = crateFile.getInt("crate-amount");
        commonItemAmount = minItems - 1;
        uncommonItemAmount = maxItems - 1;
        rareItemAmount = 1;
        for (String rarityLabel : crateFile.getConfigurationSection("items").getKeys(false)) {
            CrateRarity rarity = CrateRarity.valueOf(rarityLabel.toUpperCase());
            crateItems.put(rarity, new ArrayList<>());
            for (String itemLabel : crateFile.getStringList("items." + rarityLabel)) {
                Material item = Material.valueOf(itemLabel.toUpperCase());
                crateItems.get(rarity).add(item);
            }
        }
    }

    public HashMap<CrateRarity, List<Material>> getCrateItems() { return crateItems; }
    public int getMinItems() { return minItems; }
    public int getMaxItems() { return maxItems; }
    public int getCommonItemAmount() { return commonItemAmount; }
    public int getUncommonItemAmount() { return uncommonItemAmount; }
    public int getRareItemAmount() { return rareItemAmount; }
    public int getCrateAmount() { return crateAmount; }
}
