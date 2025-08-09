package com.tanner.minigames.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

public class ScoreboardBuilder {

    private Scoreboard board;
    private Objective obj;
    private HashMap<Integer, String> scoreboardLines;
    private HashMap<Integer, ScoreboardTeam> scoreboardTeams;

    public ScoreboardBuilder(String scoreboardName, String scoreboardDisplayName, HashMap<Integer, String> scoreboardLines, HashMap<Integer, ScoreboardTeam> scoreboardTeams) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        obj = board.registerNewObjective(ChatColor.stripColor(scoreboardName.toLowerCase()), "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(scoreboardDisplayName);
        this.scoreboardLines = scoreboardLines;
        this.scoreboardTeams = scoreboardTeams;

        buildScoreboard();
    }

    private void buildScoreboard() {
        int highestLineNumber = getScoreboardLineAmount();

        for (int i = 0; i <= highestLineNumber; i++) {
            if (scoreboardLines.containsKey(i)) {
                Score score = obj.getScore(scoreboardLines.get(i));
                score.setScore(i);
            } else if (scoreboardTeams.containsKey(i)) {
                ScoreboardTeam scoreboardTeam = scoreboardTeams.get(i);
                Team team = board.registerNewTeam(scoreboardTeam.getName());
                String randomEntryName = ChatColor.values()[i].toString();
                team.addEntry(randomEntryName);
                team.setPrefix(scoreboardTeam.getPrefix());
                team.setSuffix(scoreboardTeam.getSuffix());
                Score score = obj.getScore(randomEntryName);
                score.setScore(i);
            } else {
                Score score = obj.getScore(Util.repeat(" ", i));
                score.setScore(i);
            }
        }
    }

    public void disablePlayerCollision() {
        Team noCollisionTeam = board.registerNewTeam("no_collision");
        noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    private int getScoreboardLineAmount() {
        int highestLineNumber = 0;
        for (int lineNumber : scoreboardLines.keySet()) {
            if (lineNumber > highestLineNumber) {
                highestLineNumber = lineNumber;
            }
        }

        for (int lineNumber : scoreboardTeams.keySet()) {
            if (lineNumber > highestLineNumber) {
                highestLineNumber = lineNumber;
            }
        }
        return  highestLineNumber;
    }

    public void updateScoreboard(String teamName, String suffix) {
        board.getTeam(teamName).setSuffix(suffix);
    }

    public Scoreboard getBoard() { return board; }
}
