package com.criczone.demo.model.teamsegregation;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class TeamSegregationTrainingResult {
    private double winRateWeight;
    private double runDifferenceWeight;
    private double wicketDifferenceWeight;
    private double experienceWeight;
    private double accuracy;
    private int trainingMatches;
    private Instant trainedAt = Instant.now();

    public double getWinRateWeight() { return winRateWeight; }
    public void setWinRateWeight(double winRateWeight) { this.winRateWeight = winRateWeight; }
    public double getRunDifferenceWeight() { return runDifferenceWeight; }
    public void setRunDifferenceWeight(double runDifferenceWeight) { this.runDifferenceWeight = runDifferenceWeight; }
    public double getWicketDifferenceWeight() { return wicketDifferenceWeight; }
    public void setWicketDifferenceWeight(double wicketDifferenceWeight) { this.wicketDifferenceWeight = wicketDifferenceWeight; }
    public double getExperienceWeight() { return experienceWeight; }
    public void setExperienceWeight(double experienceWeight) { this.experienceWeight = experienceWeight; }
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    public int getTrainingMatches() { return trainingMatches; }
    public void setTrainingMatches(int trainingMatches) { this.trainingMatches = trainingMatches; }
    public Instant getTrainedAt() { return trainedAt; }
    public void setTrainedAt(Instant trainedAt) { this.trainedAt = trainedAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("winRateWeight", winRateWeight);
        data.put("runDifferenceWeight", runDifferenceWeight);
        data.put("wicketDifferenceWeight", wicketDifferenceWeight);
        data.put("experienceWeight", experienceWeight);
        data.put("accuracy", accuracy);
        data.put("trainingMatches", trainingMatches);
        data.put("trainedAt", trainedAt);
        return data;
    }
}
