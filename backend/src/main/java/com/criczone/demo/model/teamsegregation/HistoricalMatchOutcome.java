package com.criczone.demo.model.teamsegregation;

public class HistoricalMatchOutcome {
    private final String teamAId;
    private final String teamBId;
    private final String winnerTeamId;

    public HistoricalMatchOutcome(String teamAId, String teamBId, String winnerTeamId) {
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.winnerTeamId = winnerTeamId;
    }

    public String getTeamAId() { return teamAId; }
    public String getTeamBId() { return teamBId; }
    public String getWinnerTeamId() { return winnerTeamId; }
}
