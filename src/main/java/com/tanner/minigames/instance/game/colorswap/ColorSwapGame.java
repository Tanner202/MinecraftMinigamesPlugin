package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import com.tanner.minigames.instance.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class ColorSwapGame extends Game {

    private Grid grid;

    private int gridSize = 25;
    private int cellSize = 5;
    private List<Material> materials = new ArrayList<>();

    public ColorSwapGame(Minigames minigames, Arena arena) {
        super(minigames, arena);
    }

    public void setMaterials() {
        materials.add(Material.WHITE_WOOL);
        materials.add(Material.RED_WOOL);
        materials.add(Material.BLUE_WOOL);
        materials.add(Material.YELLOW_WOOL);
        materials.add(Material.GREEN_WOOL);
    }

    @Override
    public void onStart() {
        setMaterials();
        Bukkit.broadcastMessage("Color Swap Initial Info: " + minigames);
        grid = new Grid(minigames, arena, arena.getSpawn(), gridSize, cellSize, materials);
    }

    @Override
    public void onEnd() {}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (arena.getPlayers().size() == 1) {
            Player winningPlayer = Bukkit.getPlayer(arena.getPlayers().getFirst());
            arena.sendMessage(ChatColor.GOLD + winningPlayer.getName() + " has Won! Thanks for Playing!");
            arena.reset(true);
        }
    }
}
