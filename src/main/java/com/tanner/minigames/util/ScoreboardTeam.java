package com.tanner.minigames.util;

public class ScoreboardTeam {

    private String name;
    private String prefix;
    private String suffix;

    public ScoreboardTeam(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}
