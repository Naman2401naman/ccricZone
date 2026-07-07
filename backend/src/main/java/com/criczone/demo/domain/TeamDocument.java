package com.criczone.demo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("teams")
public class TeamDocument {
    @Id
    private String id;
    private String name;
    private String tournament;
    private String owner;
    private List<Map<String, Object>> members = new ArrayList<>();
    private Map<String, Object> stats = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public TeamDocument() {
        stats.put("matchesPlayed", 0);
        stats.put("wins", 0);
        stats.put("losses", 0);
        stats.put("draws", 0);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTournament() { return tournament; }
    public void setTournament(String tournament) { this.tournament = tournament; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public List<Map<String, Object>> getMembers() { return members; }
    public void setMembers(List<Map<String, Object>> members) { this.members = members; }
    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
