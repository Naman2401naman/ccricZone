package com.criczone.demo.model.teamsegregation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamSegregationBucket {
    private String name;
    private List<TeamPerformanceStats> teams = new ArrayList<>();
    private double averageScore;

    public TeamSegregationBucket(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<TeamPerformanceStats> getTeams() { return teams; }
    public void setTeams(List<TeamPerformanceStats> teams) { this.teams = teams; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public void addTeam(TeamPerformanceStats team) {
        teams.add(team);
        double total = teams.stream().mapToDouble(TeamPerformanceStats::getPerformanceScore).sum();
        averageScore = teams.isEmpty() ? 0 : Math.round((total / teams.size()) * 100d) / 100d;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("averageScore", averageScore);
        data.put("teamCount", teams.size());
        data.put("teams", teams.stream().map(TeamPerformanceStats::toMap).collect(Collectors.toList()));
        return data;
    }
}
