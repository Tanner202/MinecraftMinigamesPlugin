package com.tanner.minigames;

import com.tanner.minigames.instance.GameType;
import org.bukkit.Location;

public class GameSettings {

    private GameType gameType;
    private Location lobbySpawn;
    private Location npcSpawn;
    private int teamSize;
    private int playerLimit;
    private boolean worldReloadEnabled;

    public GameSettings(GameType gameType, Location lobbySpawn, Location npcSpawn, int teamSize, int playerLimit, boolean worldReloadEnabled) {
        this.gameType = gameType;
        this.lobbySpawn = lobbySpawn;
        this.npcSpawn = npcSpawn;
        this.teamSize = teamSize;
        this.playerLimit = playerLimit;
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

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public boolean isWorldReloadEnabled() {
        return worldReloadEnabled;
    }

    public void setWorldReloadEnabled(boolean worldReloadEnabled) {
        this.worldReloadEnabled = worldReloadEnabled;
    }
}
