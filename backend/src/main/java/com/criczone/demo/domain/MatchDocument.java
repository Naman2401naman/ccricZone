package com.criczone.demo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("matches")
public class MatchDocument {
    @Id
    private String id;
    private String matchName;
    private String matchType;
    private Integer totalOvers;
    private Integer ballsPerOver = 6;
    private Map<String, Object> teamA = new LinkedHashMap<>();
    private Map<String, Object> teamB = new LinkedHashMap<>();
    private String venue;
    private Instant matchDate;
    private String status = "scheduled";
    private String winner;
    private String resultType;
    private Integer resultMargin;
    private String createdBy;
    private String tournament;
    private Map<String, Object> toss = new LinkedHashMap<>();
    private Integer currentInning = 1;
    private String currentStriker;
    private String currentStrikerId;
    private String currentNonStriker;
    private String currentNonStrikerId;
    private String currentBowler;
    private String currentBowlerId;
    private Map<String, Object> currentOver = new LinkedHashMap<>();
    private Boolean statsProcessed = false;
    private Map<String, Object> innings = new LinkedHashMap<>();
    private List<Map<String, Object>> ballByBallData = new ArrayList<>();
    private List<Map<String, Object>> batsmanStats = new ArrayList<>();
    private List<Map<String, Object>> bowlerStats = new ArrayList<>();
    private List<Map<String, Object>> fallOfWickets = new ArrayList<>();
    private List<Map<String, Object>> partnerships = new ArrayList<>();
    private List<Map<String, Object>> overSummary = new ArrayList<>();
    private Map<String, Object> analytics = new LinkedHashMap<>();
    private Map<String, Object> media = new LinkedHashMap<>();
    private Map<String, Object> engagement = new LinkedHashMap<>();
    private Map<String, Object> manOfTheMatch = new LinkedHashMap<>();
    private Map<String, Object> officials = new LinkedHashMap<>();
    private Map<String, Object> conditions = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public MatchDocument() {
        currentOver.put("overNumber", 0);
        currentOver.put("ballNumber", 0);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }
    public Integer getBallsPerOver() { return ballsPerOver; }
    public void setBallsPerOver(Integer ballsPerOver) { this.ballsPerOver = ballsPerOver; }
    public Map<String, Object> getTeamA() { return teamA; }
    public void setTeamA(Map<String, Object> teamA) { this.teamA = teamA; }
    public Map<String, Object> getTeamB() { return teamB; }
    public void setTeamB(Map<String, Object> teamB) { this.teamB = teamB; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public Instant getMatchDate() { return matchDate; }
    public void setMatchDate(Instant matchDate) { this.matchDate = matchDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public Integer getResultMargin() { return resultMargin; }
    public void setResultMargin(Integer resultMargin) { this.resultMargin = resultMargin; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getTournament() { return tournament; }
    public void setTournament(String tournament) { this.tournament = tournament; }
    public Map<String, Object> getToss() { return toss; }
    public void setToss(Map<String, Object> toss) { this.toss = toss; }
    public Integer getCurrentInning() { return currentInning; }
    public void setCurrentInning(Integer currentInning) { this.currentInning = currentInning; }
    public String getCurrentStriker() { return currentStriker; }
    public void setCurrentStriker(String currentStriker) { this.currentStriker = currentStriker; }
    public String getCurrentStrikerId() { return currentStrikerId; }
    public void setCurrentStrikerId(String currentStrikerId) { this.currentStrikerId = currentStrikerId; }
    public String getCurrentNonStriker() { return currentNonStriker; }
    public void setCurrentNonStriker(String currentNonStriker) { this.currentNonStriker = currentNonStriker; }
    public String getCurrentNonStrikerId() { return currentNonStrikerId; }
    public void setCurrentNonStrikerId(String currentNonStrikerId) { this.currentNonStrikerId = currentNonStrikerId; }
    public String getCurrentBowler() { return currentBowler; }
    public void setCurrentBowler(String currentBowler) { this.currentBowler = currentBowler; }
    public String getCurrentBowlerId() { return currentBowlerId; }
    public void setCurrentBowlerId(String currentBowlerId) { this.currentBowlerId = currentBowlerId; }
    public Map<String, Object> getCurrentOver() { return currentOver; }
    public void setCurrentOver(Map<String, Object> currentOver) { this.currentOver = currentOver; }
    public Boolean getStatsProcessed() { return statsProcessed; }
    public void setStatsProcessed(Boolean statsProcessed) { this.statsProcessed = statsProcessed; }
    public Map<String, Object> getInnings() { return innings; }
    public void setInnings(Map<String, Object> innings) { this.innings = innings; }
    public List<Map<String, Object>> getBallByBallData() { return ballByBallData; }
    public void setBallByBallData(List<Map<String, Object>> ballByBallData) { this.ballByBallData = ballByBallData; }
    public List<Map<String, Object>> getBatsmanStats() { return batsmanStats; }
    public void setBatsmanStats(List<Map<String, Object>> batsmanStats) { this.batsmanStats = batsmanStats; }
    public List<Map<String, Object>> getBowlerStats() { return bowlerStats; }
    public void setBowlerStats(List<Map<String, Object>> bowlerStats) { this.bowlerStats = bowlerStats; }
    public List<Map<String, Object>> getFallOfWickets() { return fallOfWickets; }
    public void setFallOfWickets(List<Map<String, Object>> fallOfWickets) { this.fallOfWickets = fallOfWickets; }
    public List<Map<String, Object>> getPartnerships() { return partnerships; }
    public void setPartnerships(List<Map<String, Object>> partnerships) { this.partnerships = partnerships; }
    public List<Map<String, Object>> getOverSummary() { return overSummary; }
    public void setOverSummary(List<Map<String, Object>> overSummary) { this.overSummary = overSummary; }
    public Map<String, Object> getAnalytics() { return analytics; }
    public void setAnalytics(Map<String, Object> analytics) { this.analytics = analytics; }
    public Map<String, Object> getMedia() { return media; }
    public void setMedia(Map<String, Object> media) { this.media = media; }
    public Map<String, Object> getEngagement() { return engagement; }
    public void setEngagement(Map<String, Object> engagement) { this.engagement = engagement; }
    public Map<String, Object> getManOfTheMatch() { return manOfTheMatch; }
    public void setManOfTheMatch(Map<String, Object> manOfTheMatch) { this.manOfTheMatch = manOfTheMatch; }
    public Map<String, Object> getOfficials() { return officials; }
    public void setOfficials(Map<String, Object> officials) { this.officials = officials; }
    public Map<String, Object> getConditions() { return conditions; }
    public void setConditions(Map<String, Object> conditions) { this.conditions = conditions; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
