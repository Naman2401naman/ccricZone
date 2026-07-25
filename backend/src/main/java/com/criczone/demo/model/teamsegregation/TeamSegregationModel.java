package com.criczone.demo.model.teamsegregation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TeamSegregationModel {

    public TeamSegregationTrainingResult defaultTraining() {
        TeamSegregationTrainingResult result = new TeamSegregationTrainingResult();
        result.setWinRateWeight(0.55d);
        result.setRunDifferenceWeight(0.25d);
        result.setWicketDifferenceWeight(0.25d);
        result.setExperienceWeight(1.0d);
        result.setAccuracy(0);
        result.setTrainingMatches(0);
        return result;
    }

    public TeamSegregationTrainingResult train(List<TeamPerformanceStats> teamStats, List<HistoricalMatchOutcome> outcomes) {
        if (teamStats.isEmpty() || outcomes.isEmpty()) {
            return defaultTraining();
        }

        Map<String, TeamPerformanceStats> statsByTeamId = new LinkedHashMap<>();
        for (TeamPerformanceStats stats : teamStats) {
            statsByTeamId.put(stats.getTeamId(), stats);
        }

        TeamSegregationTrainingResult best = defaultTraining();
        double bestAccuracy = -1;
        double[] candidates = new double[] {0.15d, 0.25d, 0.35d, 0.45d, 0.55d, 0.65d};
        for (double winWeight : candidates) {
            for (double runWeight : candidates) {
                for (double wicketWeight : candidates) {
                    double accuracy = accuracy(statsByTeamId, outcomes, winWeight, runWeight, wicketWeight, 1.0d);
                    if (accuracy > bestAccuracy) {
                        bestAccuracy = accuracy;
                        best.setWinRateWeight(winWeight);
                        best.setRunDifferenceWeight(runWeight);
                        best.setWicketDifferenceWeight(wicketWeight);
                        best.setExperienceWeight(1.0d);
                        best.setAccuracy(round(accuracy));
                        best.setTrainingMatches(outcomes.size());
                    }
                }
            }
        }
        return best;
    }

    public double score(TeamPerformanceStats stats, TeamSegregationTrainingResult training) {
        if (stats.getMatchesPlayed() == 0) return 0;
        double winComponent = stats.getWinRate() * training.getWinRateWeight();
        double runDiffComponent = (clamp(stats.getAverageRunDifference(), -50, 50) + 50) * training.getRunDifferenceWeight();
        double wicketComponent = (clamp((stats.getWicketsTaken() - stats.getWicketsLost()) * 2d, -20, 20) + 20) * training.getWicketDifferenceWeight();
        double experienceComponent = Math.min(10, stats.getMatchesPlayed() * 2d) * training.getExperienceWeight();
        return round(winComponent + runDiffComponent + wicketComponent + experienceComponent);
    }

    public List<TeamSegregationBucket> segregate(List<TeamPerformanceStats> rankedTeams, int bucketCount) {
        int safeBucketCount = Math.max(2, Math.min(8, bucketCount));
        List<TeamPerformanceStats> ordered = new ArrayList<>(rankedTeams);
        ordered.sort(Comparator.comparing(TeamPerformanceStats::getPerformanceScore).reversed());

        List<TeamSegregationBucket> buckets = new ArrayList<>();
        for (int i = 0; i < safeBucketCount; i++) {
            buckets.add(new TeamSegregationBucket("Group " + (i + 1)));
        }

        for (int i = 0; i < ordered.size(); i++) {
            int round = i / safeBucketCount;
            int slot = i % safeBucketCount;
            int bucketIndex = round % 2 == 0 ? slot : safeBucketCount - 1 - slot;
            buckets.get(bucketIndex).addTeam(ordered.get(i));
        }
        return buckets;
    }

    public String tierFor(double score) {
        if (score >= 75) return "elite";
        if (score >= 55) return "strong";
        if (score >= 35) return "balanced";
        return "developing";
    }

    private double accuracy(Map<String, TeamPerformanceStats> statsByTeamId,
                            List<HistoricalMatchOutcome> outcomes,
                            double winWeight,
                            double runWeight,
                            double wicketWeight,
                            double experienceWeight) {
        TeamSegregationTrainingResult trial = new TeamSegregationTrainingResult();
        trial.setWinRateWeight(winWeight);
        trial.setRunDifferenceWeight(runWeight);
        trial.setWicketDifferenceWeight(wicketWeight);
        trial.setExperienceWeight(experienceWeight);

        int eligible = 0;
        int correct = 0;
        for (HistoricalMatchOutcome outcome : outcomes) {
            TeamPerformanceStats teamA = statsByTeamId.get(outcome.getTeamAId());
            TeamPerformanceStats teamB = statsByTeamId.get(outcome.getTeamBId());
            if (teamA == null || teamB == null || outcome.getWinnerTeamId() == null || outcome.getWinnerTeamId().isBlank()) {
                continue;
            }
            eligible++;
            String predictedWinner = score(teamA, trial) >= score(teamB, trial) ? teamA.getTeamId() : teamB.getTeamId();
            if (Objects.equals(predictedWinner, outcome.getWinnerTeamId())) {
                correct++;
            }
        }
        return eligible == 0 ? 0 : (correct * 100d) / eligible;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    private double round(double value) {
        return Math.round(value * 100d) / 100d;
    }
}
