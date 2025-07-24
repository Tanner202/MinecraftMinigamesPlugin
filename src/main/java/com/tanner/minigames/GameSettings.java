package com.tanner.minigames;

import com.tanner.minigames.instance.GameType;
import org.bukkit.Location;

public class GameSettings {

    private GameType gameType;
    private Location lobbySpawn;
    private Location npcSpawn;
    private int teamAmount;
    private int maxPlayerAmount;
    private boolean worldReloadEnabled;

    public GameSettings(GameType gameType, Location lobbySpawn, Location npcSpawn, int teamAmount, int maxPlayerAmount, boolean worldReloadEnabled) {
        this.gameType = gameType;
        this.lobbySpawn = lobbySpawn;
        this.npcSpawn = npcSpawn;
        this.teamAmount = teamAmount;
        this.maxPlayerAmount = maxPlayerAmount;
        this.worldReloadEnabled = worldReloadEnabled;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public Location getNpcSpawn() {
        return npcSpawn;
    }

    public void setNpcSpawn(Location npcSpawn) {
        this.npcSpawn = npcSpawn;
    }

    public int getTeamAmount() {
        return teamAmount;
    }

    public void setTeamAmount(int teamAmount) {
        this.teamAmount = teamAmount;
    }

    public int getMaxPlayerAmount() {
        return maxPlayerAmount;
    }

    public void setMaxPlayerAmount(int maxPlayerAmount) {
        this.maxPlayerAmount = maxPlayerAmount;
    }

    public boolean isWorldReloadEnabled() {
        return worldReloadEnabled;
    }

    public void setWorldReloadEnabled(boolean worldReloadEnabled) {
        this.worldReloadEnabled = worldReloadEnabled;
    }
}
