package com.criczone.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public class UserDocument {

    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String phone;
    @JsonIgnore
    private String password;
    private String role = "user";
    private Map<String, Object> profile = new LinkedHashMap<>();
    private Map<String, Object> stats = new LinkedHashMap<>();
    private Map<String, Object> formatStats = new LinkedHashMap<>();
    private List<Map<String, Object>> teams = new ArrayList<>();
    private List<Map<String, Object>> tournaments = new ArrayList<>();
    private List<Map<String, Object>> matchHistory = new ArrayList<>();
    private Map<String, Object> social = new LinkedHashMap<>();
    private Map<String, Object> media = new LinkedHashMap<>();
    private Map<String, Object> rankings = new LinkedHashMap<>();
    private Map<String, Object> notifications = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public UserDocument() {
        social.put("followers", new ArrayList<>());
        social.put("following", new ArrayList<>());
        media.put("profilePicture", "https://via.placeholder.com/150");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Map<String, Object> getProfile() { return profile; }
    public void setProfile(Map<String, Object> profile) { this.profile = profile; }
    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }
    public Map<String, Object> getFormatStats() { return formatStats; }
    public void setFormatStats(Map<String, Object> formatStats) { this.formatStats = formatStats; }
    public List<Map<String, Object>> getTeams() { return teams; }
    public void setTeams(List<Map<String, Object>> teams) { this.teams = teams; }
    public List<Map<String, Object>> getTournaments() { return tournaments; }
    public void setTournaments(List<Map<String, Object>> tournaments) { this.tournaments = tournaments; }
    public List<Map<String, Object>> getMatchHistory() { return matchHistory; }
    public void setMatchHistory(List<Map<String, Object>> matchHistory) { this.matchHistory = matchHistory; }
    public Map<String, Object> getSocial() { return social; }
    public void setSocial(Map<String, Object> social) { this.social = social; }
    public Map<String, Object> getMedia() { return media; }
    public void setMedia(Map<String, Object> media) { this.media = media; }
    public Map<String, Object> getRankings() { return rankings; }
    public void setRankings(Map<String, Object> rankings) { this.rankings = rankings; }
    public Map<String, Object> getNotifications() { return notifications; }
    public void setNotifications(Map<String, Object> notifications) { this.notifications = notifications; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
