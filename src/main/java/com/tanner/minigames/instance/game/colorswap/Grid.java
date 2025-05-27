package com.tanner.minigames.instance.game.colorswap;

import com.tanner.minigames.Minigames;
import com.tanner.minigames.instance.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Grid {

    private Minigames minigames;

    private Location startingLocation;
    private Arena arena;
    private int gridSize;
    private int cellSize;

    private Material chosenMaterial;

    private List<Material> remainingColors = new ArrayList<>();

    private long timeBetweenColorSwaps = 5;
    private long timeBetweenRemovingWool = 5;

    BukkitTask generateGridTask;
    BukkitTask removeGridTask;

    public Grid(Minigames minigames, Arena arena, Location arenaSpawn, int gridSize, int cellSize) {
        this.arena = arena;

        // Setting starting location away from arena spawn so arena spawn is in center of grid
        startingLocation = new Location(arenaSpawn.getWorld(),
                arenaSpawn.getX() - (double) gridSize /2,
                arenaSpawn.getY() - 1,
                arenaSpawn.getZ() - (double) gridSize/2,
                0, 0);
        this.minigames = minigames;
        this.cellSize = cellSize;
        this.gridSize = gridSize;

        if (gridSize % cellSize != 0) {
            matchGridSizeToCellSize();
        }

        setGrid();
    }

    public void Stop() {
        if (generateGridTask != null) {
            generateGridTask.cancel();
        }

        if (removeGridTask != null) {
            removeGridTask.cancel();
        }
    }

    private void matchGridSizeToCellSize() {
        gridSize = (int) Math.sqrt(getCellAmount()) * cellSize;
    }

    private void setGrid() {
        addRemainingColors();

        for (double x = startingLocation.getX(); x + cellSize <= startingLocation.getX() + gridSize; x += cellSize) {
            for (double z = startingLocation.getZ(); z + cellSize <= startingLocation.getZ() + gridSize; z += cellSize) {
                generateCell(x, z);
            }
        }

        chooseRandomWool();

        removeGridTask = Bukkit.getScheduler().runTaskLater(minigames, this::removeUnchosenWool, timeBetweenRemovingWool * 20);
    }

    private void chooseRandomWool() {
        Random random = new Random();
        int randomIndex = random.nextInt(0, GridColor.values().length);
        GridColor gridColor = GridColor.values()[randomIndex];
        chosenMaterial = gridColor.getMaterial();

        arena.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Go to " + gridColor.getDisplay());
    }

    private void removeUnchosenWool() {
        for (double x = startingLocation.getX(); x <= startingLocation.getX() + gridSize; x++) {
            for (double z = startingLocation.getZ(); z <= startingLocation.getZ() + gridSize; z++) {
                Location location = new Location(startingLocation.getWorld(), x, startingLocation.getY(), z);
                if (!location.getBlock().getType().equals(chosenMaterial)) {
                    location.getBlock().setType(Material.AIR);
                }
            }
        }

        generateGridTask = Bukkit.getScheduler().runTaskLater(minigames, this::setGrid, timeBetweenColorSwaps * 20);
    }

    private int getCellAmount() {
        int count = 0;
        for (int i = 0; i + cellSize <= gridSize; i += cellSize) {
            count++;
        }
        return (int) Math.pow(count, 2);
    }

    private void addRemainingColors() {
        remainingColors.clear();
        for (int i = 0; i < getCellAmount(); i++) {
            Material materialToAdd = GridColor.values()[i%GridColor.values().length].getMaterial();
            remainingColors.add(materialToAdd);
        }
    }

    private void generateCell(double startingX, double startingZ) {
        Random random = new Random();
        int randomIndex = random.nextInt(0, remainingColors.size());
        Material randomWool = remainingColors.get(randomIndex);
        remainingColors.remove(randomIndex);
        for (double x = startingX; x < startingX + cellSize; x++) {
            for (double z = startingZ; z < startingZ + cellSize; z++) {
                Location location = new Location(startingLocation.getWorld(), x, startingLocation.getY(), z);
                location.getBlock().setType(randomWool);
            }
        }
    }
}
