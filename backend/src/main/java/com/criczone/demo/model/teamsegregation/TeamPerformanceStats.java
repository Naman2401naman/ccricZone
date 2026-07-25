package com.criczone.demo.model.teamsegregation;

import java.util.LinkedHashMap;
import java.util.Map;

public class TeamPerformanceStats {
    private String teamId;
    private String teamName;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private int runsFor;
    private int runsAgainst;
    private int wicketsLost;
    private int wicketsTaken;
    private double winRate;
    private double averageRunDifference;
    private double performanceScore;
    private String tier;

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }
    public int getRunsFor() { return runsFor; }
    public void setRunsFor(int runsFor) { this.runsFor = runsFor; }
    public int getRunsAgainst() { return runsAgainst; }
    public void setRunsAgainst(int runsAgainst) { this.runsAgainst = runsAgainst; }
    public int getWicketsLost() { return wicketsLost; }
    public void setWicketsLost(int wicketsLost) { this.wicketsLost = wicketsLost; }
    public int getWicketsTaken() { return wicketsTaken; }
    public void setWicketsTaken(int wicketsTaken) { this.wicketsTaken = wicketsTaken; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public double getAverageRunDifference() { return averageRunDifference; }
    public void setAverageRunDifference(double averageRunDifference) { this.averageRunDifference = averageRunDifference; }
    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teamId", teamId);
        data.put("teamName", teamName);
        data.put("matchesPlayed", matchesPlayed);
        data.put("wins", wins);
        data.put("losses", losses);
        data.put("draws", draws);
        data.put("runsFor", runsFor);
        data.put("runsAgainst", runsAgainst);
        data.put("wicketsLost", wicketsLost);
        data.put("wicketsTaken", wicketsTaken);
        data.put("winRate", winRate);
        data.put("averageRunDifference", averageRunDifference);
        data.put("performanceScore", performanceScore);
        data.put("tier", tier);
        return data;
    }
}
