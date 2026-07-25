package com.criczone.demo.service;

import com.criczone.demo.domain.MatchDocument;
import com.criczone.demo.domain.TeamDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.model.teamsegregation.HistoricalMatchOutcome;
import com.criczone.demo.model.teamsegregation.TeamPerformanceStats;
import com.criczone.demo.model.teamsegregation.TeamSegregationBucket;
import com.criczone.demo.model.teamsegregation.TeamSegregationModel;
import com.criczone.demo.model.teamsegregation.TeamSegregationTrainingResult;
import com.criczone.demo.repo.MatchRepository;
import com.criczone.demo.repo.TeamRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TeamSegregationService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamSegregationModel segregationModel = new TeamSegregationModel();

    public TeamSegregationService(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public Map<String, Object> segregate(UserDocument currentUser, Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String venue = ApiSupport.trim(request == null ? null : request.get("venue"));
        int bucketCount = clamp(ApiSupport.safeInt(request == null ? null : request.get("bucketCount")), 2, 8);
        int minimumMatches = Math.max(0, ApiSupport.safeInt(request == null ? null : request.get("minimumMatches")));

        List<TeamDocument> teams = resolveTeams(user, request);
        if (teams.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least two teams are required for segregation");
        }

        List<MatchDocument> matches = matchRepository.findAll().stream()
            .filter(match -> "completed".equalsIgnoreCase(String.valueOf(match.getStatus())))
            .filter(match -> venue.isBlank() || venue.equalsIgnoreCase(ApiSupport.trim(match.getVenue())))
            .collect(Collectors.toList());

        List<TeamPerformanceStats> rankedTeams = teams.stream()
            .map(team -> buildStats(team, matches, segregationModel.defaultTraining()))
            .collect(Collectors.toList());
        TeamSegregationTrainingResult training = segregationModel.train(rankedTeams, buildOutcomes(matches));
        rankedTeams = rankedTeams.stream()
            .peek(stats -> applyScore(stats, training))
            .filter(stats -> stats.getMatchesPlayed() >= minimumMatches)
            .sorted(Comparator.comparing(TeamPerformanceStats::getPerformanceScore).reversed())
            .collect(Collectors.toList());

        if (rankedTeams.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough teams with matching past turf performance");
        }

        List<TeamSegregationBucket> buckets = segregationModel.segregate(rankedTeams, Math.min(bucketCount, rankedTeams.size()));
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("venue", venue.isBlank() ? "all" : venue);
        filters.put("minimumMatches", minimumMatches);
        filters.put("bucketCount", buckets.size());

        return ApiSupport.mapOf(
            "success", true,
            "message", "Teams segregated by past turf performance",
            "filters", filters,
            "model", training.toMap(),
            "teamCount", rankedTeams.size(),
            "rankedTeams", rankedTeams.stream().map(TeamPerformanceStats::toMap).collect(Collectors.toList()),
            "groups", buckets.stream().map(TeamSegregationBucket::toMap).collect(Collectors.toList())
        );
    }

    public Map<String, Object> train(UserDocument currentUser, Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String venue = ApiSupport.trim(request == null ? null : request.get("venue"));
        List<TeamDocument> teams = resolveTeams(user, request);
        List<MatchDocument> matches = matchRepository.findAll().stream()
            .filter(match -> "completed".equalsIgnoreCase(String.valueOf(match.getStatus())))
            .filter(match -> venue.isBlank() || venue.equalsIgnoreCase(ApiSupport.trim(match.getVenue())))
            .collect(Collectors.toList());

        List<TeamPerformanceStats> baseStats = teams.stream()
            .map(team -> buildStats(team, matches, segregationModel.defaultTraining()))
            .collect(Collectors.toList());
        TeamSegregationTrainingResult training = segregationModel.train(baseStats, buildOutcomes(matches));
        List<Map<String, Object>> trainedStats = baseStats.stream()
            .peek(stats -> applyScore(stats, training))
            .sorted(Comparator.comparing(TeamPerformanceStats::getPerformanceScore).reversed())
            .map(TeamPerformanceStats::toMap)
            .collect(Collectors.toList());

        return ApiSupport.mapOf(
            "success", true,
            "message", "Team segregation model trained from completed match history",
            "venue", venue.isBlank() ? "all" : venue,
            "model", training.toMap(),
            "teamsAnalyzed", trainedStats.size(),
            "data", trainedStats
        );
    }

    public Map<String, Object> performance(UserDocument currentUser, String venue) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        Map<String, Object> request = ApiSupport.mapOf("venue", venue, "bucketCount", 2, "minimumMatches", 0);
        List<TeamDocument> teams = resolveTeams(user, request);
        List<MatchDocument> matches = matchRepository.findAll().stream()
            .filter(match -> "completed".equalsIgnoreCase(String.valueOf(match.getStatus())))
            .filter(match -> ApiSupport.trim(venue).isBlank() || ApiSupport.trim(venue).equalsIgnoreCase(ApiSupport.trim(match.getVenue())))
            .collect(Collectors.toList());
        TeamSegregationTrainingResult training = segregationModel.train(
            teams.stream().map(team -> buildStats(team, matches, segregationModel.defaultTraining())).collect(Collectors.toList()),
            buildOutcomes(matches)
        );
        List<Map<String, Object>> data = teams.stream()
            .map(team -> buildStats(team, matches, training))
            .sorted(Comparator.comparing(TeamPerformanceStats::getPerformanceScore).reversed())
            .map(TeamPerformanceStats::toMap)
            .collect(Collectors.toList());
        return Map.of("success", true, "model", training.toMap(), "count", data.size(), "data", data);
    }

    private List<TeamDocument> resolveTeams(UserDocument user, Map<String, Object> request) {
        Object rawTeamIds = request == null ? null : request.get("teamIds");
        if (rawTeamIds instanceof List<?>) {
            List<String> ids = ((List<?>) rawTeamIds).stream()
                .map(ApiSupport::trim)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                Set<String> allowedIds = ids.stream().collect(Collectors.toSet());
                List<TeamDocument> selectedTeams = new ArrayList<>();
                teamRepository.findAllById(ids).forEach(selectedTeams::add);
                return selectedTeams.stream()
                    .filter(team -> allowedIds.contains(team.getId()))
                    .collect(Collectors.toList());
            }
        }

        return teamRepository.findAll().stream()
            .filter(team -> Objects.equals(team.getOwner(), user.getId())
                || team.getMembers().stream().anyMatch(member -> Objects.equals(String.valueOf(member.get("player")), user.getId())))
            .collect(Collectors.toList());
    }

    private TeamPerformanceStats buildStats(TeamDocument team, List<MatchDocument> matches, TeamSegregationTrainingResult training) {
        TeamPerformanceStats stats = new TeamPerformanceStats();
        stats.setTeamId(team.getId());
        stats.setTeamName(team.getName());

        for (MatchDocument match : matches) {
            Map<String, Object> own = teamSide(match, team.getId());
            if (own == null) continue;
            Map<String, Object> opponent = own == match.getTeamA() ? match.getTeamB() : match.getTeamA();

            stats.setMatchesPlayed(stats.getMatchesPlayed() + 1);
            stats.setRunsFor(stats.getRunsFor() + ApiSupport.safeInt(own.get("score")));
            stats.setRunsAgainst(stats.getRunsAgainst() + ApiSupport.safeInt(opponent.get("score")));
            stats.setWicketsLost(stats.getWicketsLost() + ApiSupport.safeInt(own.get("wickets")));
            stats.setWicketsTaken(stats.getWicketsTaken() + ApiSupport.safeInt(opponent.get("wickets")));

            String result = resultFor(match, own, opponent, team);
            if ("win".equals(result)) {
                stats.setWins(stats.getWins() + 1);
            } else if ("loss".equals(result)) {
                stats.setLosses(stats.getLosses() + 1);
            } else {
                stats.setDraws(stats.getDraws() + 1);
            }
        }

        int played = Math.max(1, stats.getMatchesPlayed());
        stats.setWinRate(round((stats.getWins() * 100d) / played));
        stats.setAverageRunDifference(round((stats.getRunsFor() - stats.getRunsAgainst()) / (double) played));
        applyScore(stats, training);
        return stats;
    }

    private void applyScore(TeamPerformanceStats stats, TeamSegregationTrainingResult training) {
        stats.setPerformanceScore(segregationModel.score(stats, training));
        stats.setTier(segregationModel.tierFor(stats.getPerformanceScore()));
    }

    private List<HistoricalMatchOutcome> buildOutcomes(List<MatchDocument> matches) {
        List<HistoricalMatchOutcome> outcomes = new ArrayList<>();
        for (MatchDocument match : matches) {
            String teamAId = ApiSupport.trim(match.getTeamA().get("teamId"));
            String teamBId = ApiSupport.trim(match.getTeamB().get("teamId"));
            if (teamAId.isBlank() || teamBId.isBlank()) continue;

            String winnerId = winnerId(match);
            if (winnerId.isBlank()) continue;
            outcomes.add(new HistoricalMatchOutcome(teamAId, teamBId, winnerId));
        }
        return outcomes;
    }

    private String winnerId(MatchDocument match) {
        String winner = ApiSupport.trim(match.getWinner());
        if (winner.equalsIgnoreCase("draw") || winner.equalsIgnoreCase("tie")) return "";
        String teamAId = ApiSupport.trim(match.getTeamA().get("teamId"));
        String teamBId = ApiSupport.trim(match.getTeamB().get("teamId"));
        if (winner.equalsIgnoreCase(teamAId) || winner.equalsIgnoreCase(ApiSupport.trim(match.getTeamA().get("name")))) return teamAId;
        if (winner.equalsIgnoreCase(teamBId) || winner.equalsIgnoreCase(ApiSupport.trim(match.getTeamB().get("name")))) return teamBId;
        int teamAScore = ApiSupport.safeInt(match.getTeamA().get("score"));
        int teamBScore = ApiSupport.safeInt(match.getTeamB().get("score"));
        if (teamAScore == teamBScore) return "";
        return teamAScore > teamBScore ? teamAId : teamBId;
    }

    private Map<String, Object> teamSide(MatchDocument match, String teamId) {
        if (matchesTeam(match.getTeamA(), teamId)) return match.getTeamA();
        if (matchesTeam(match.getTeamB(), teamId)) return match.getTeamB();
        return null;
    }

    private boolean matchesTeam(Map<String, Object> teamPayload, String teamId) {
        return Objects.equals(ApiSupport.trim(teamPayload.get("teamId")), teamId);
    }

    private String resultFor(MatchDocument match, Map<String, Object> own, Map<String, Object> opponent, TeamDocument team) {
        String winner = ApiSupport.trim(match.getWinner());
        if (winner.isBlank()) {
            int ownScore = ApiSupport.safeInt(own.get("score"));
            int opponentScore = ApiSupport.safeInt(opponent.get("score"));
            if (ownScore == opponentScore) return "draw";
            return ownScore > opponentScore ? "win" : "loss";
        }

        if (winner.equalsIgnoreCase("draw") || winner.equalsIgnoreCase("tie")) return "draw";
        if (winner.equalsIgnoreCase(team.getId()) || winner.equalsIgnoreCase(team.getName())) return "win";
        if (winner.equalsIgnoreCase(ApiSupport.trim(own.get("teamId"))) || winner.equalsIgnoreCase(ApiSupport.trim(own.get("name")))) return "win";
        return "loss";
    }

    private int clamp(int value, int min, int max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }
}
