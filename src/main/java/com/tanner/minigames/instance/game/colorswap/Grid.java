package com.tanner.minigames.instance.game.colorswap;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Grid {

    private Location startingLocation;
    private int gridSize;
    private int cellSize;

    private List<Material> remainingColors = new ArrayList<>();

    public Grid(Location arenaSpawn, int gridSize, int cellSize) {

        // Setting starting location away from arena spawn so arena spawn is in center of grid
        startingLocation = new Location(arenaSpawn.getWorld(),
                arenaSpawn.getX() - (double) gridSize / 2,
                arenaSpawn.getY() - 1,
                arenaSpawn.getZ() - (double) gridSize / 2,
                0, 0);
        this.cellSize = cellSize;
        this.gridSize = gridSize;

        if (gridSize % cellSize != 0) {
            matchGridSizeToCellSize();
        }

        setGrid();
    }

    private void matchGridSizeToCellSize() {
        gridSize = (int) Math.sqrt(getCellAmount()) * cellSize;
    }

    public void setGrid() {
        addRemainingColors();

        for (double x = startingLocation.getX(); x + cellSize <= startingLocation.getX() + gridSize; x += cellSize) {
            for (double z = startingLocation.getZ(); z + cellSize <= startingLocation.getZ() + gridSize; z += cellSize) {
                generateCell(x, z);
            }
        }
    }

    public void removeUnchosenWool(Material chosenMaterial) {
        for (double x = startingLocation.getX(); x <= startingLocation.getX() + gridSize; x++) {
            for (double z = startingLocation.getZ(); z <= startingLocation.getZ() + gridSize; z++) {
                Location location = new Location(startingLocation.getWorld(), x, startingLocation.getY(), z);
                if (!location.getBlock().getType().equals(chosenMaterial)) {
                    location.getBlock().setType(Material.AIR);
                }
            }
        }
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
