package com.criczone.demo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tournaments")
public class TournamentDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private String shortName;
    private String logo;
    private Instant startDate;
    private Instant endDate;
    private Instant registrationDeadline;
    private String venue;
    private List<Map<String, Object>> venues = new ArrayList<>();
    private String format = "T20";
    private Integer customOvers;
    private String tournamentType = "league_knockout";
    private Integer maxTeams = 8;
    private Integer minPlayers = 11;
    private Integer maxPlayers = 15;
    private List<Map<String, Object>> registeredTeams = new ArrayList<>();
    private String status = "registration_open";
    private Map<String, Object> pointsSystem = new LinkedHashMap<>();
    private List<Map<String, Object>> standings = new ArrayList<>();
    private List<Map<String, Object>> schedule = new ArrayList<>();
    private List<Map<String, Object>> groups = new ArrayList<>();
    private Map<String, Object> knockout = new LinkedHashMap<>();
    private List<String> matches = new ArrayList<>();
    private Map<String, Object> winner = new LinkedHashMap<>();
    private Map<String, Object> runnerUp = new LinkedHashMap<>();
    private Map<String, Object> awards = new LinkedHashMap<>();
    private Map<String, Object> prizePool = new LinkedHashMap<>();
    private List<Map<String, Object>> sponsors = new ArrayList<>();
    private Map<String, Object> statistics = new LinkedHashMap<>();
    private Map<String, Object> rules = new LinkedHashMap<>();
    private String createdBy;
    private List<Map<String, Object>> organizers = new ArrayList<>();
    private Map<String, Object> media = new LinkedHashMap<>();
    private Map<String, Object> social = new LinkedHashMap<>();
    private Map<String, Object> live = new LinkedHashMap<>();
    private Map<String, Object> registration = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public Instant getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(Instant registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public List<Map<String, Object>> getVenues() { return venues; }
    public void setVenues(List<Map<String, Object>> venues) { this.venues = venues; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Integer getCustomOvers() { return customOvers; }
    public void setCustomOvers(Integer customOvers) { this.customOvers = customOvers; }
    public String getTournamentType() { return tournamentType; }
    public void setTournamentType(String tournamentType) { this.tournamentType = tournamentType; }
    public Integer getMaxTeams() { return maxTeams; }
    public void setMaxTeams(Integer maxTeams) { this.maxTeams = maxTeams; }
    public Integer getMinPlayers() { return minPlayers; }
    public void setMinPlayers(Integer minPlayers) { this.minPlayers = minPlayers; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
    public List<Map<String, Object>> getRegisteredTeams() { return registeredTeams; }
    public void setRegisteredTeams(List<Map<String, Object>> registeredTeams) { this.registeredTeams = registeredTeams; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getPointsSystem() { return pointsSystem; }
    public void setPointsSystem(Map<String, Object> pointsSystem) { this.pointsSystem = pointsSystem; }
    public List<Map<String, Object>> getStandings() { return standings; }
    public void setStandings(List<Map<String, Object>> standings) { this.standings = standings; }
    public List<Map<String, Object>> getSchedule() { return schedule; }
    public void setSchedule(List<Map<String, Object>> schedule) { this.schedule = schedule; }
    public List<Map<String, Object>> getGroups() { return groups; }
    public void setGroups(List<Map<String, Object>> groups) { this.groups = groups; }
    public Map<String, Object> getKnockout() { return knockout; }
    public void setKnockout(Map<String, Object> knockout) { this.knockout = knockout; }
    public List<String> getMatches() { return matches; }
    public void setMatches(List<String> matches) { this.matches = matches; }
    public Map<String, Object> getWinner() { return winner; }
    public void setWinner(Map<String, Object> winner) { this.winner = winner; }
    public Map<String, Object> getRunnerUp() { return runnerUp; }
    public void setRunnerUp(Map<String, Object> runnerUp) { this.runnerUp = runnerUp; }
    public Map<String, Object> getAwards() { return awards; }
    public void setAwards(Map<String, Object> awards) { this.awards = awards; }
    public Map<String, Object> getPrizePool() { return prizePool; }
    public void setPrizePool(Map<String, Object> prizePool) { this.prizePool = prizePool; }
    public List<Map<String, Object>> getSponsors() { return sponsors; }
    public void setSponsors(List<Map<String, Object>> sponsors) { this.sponsors = sponsors; }
    public Map<String, Object> getStatistics() { return statistics; }
    public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }
    public Map<String, Object> getRules() { return rules; }
    public void setRules(Map<String, Object> rules) { this.rules = rules; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<Map<String, Object>> getOrganizers() { return organizers; }
    public void setOrganizers(List<Map<String, Object>> organizers) { this.organizers = organizers; }
    public Map<String, Object> getMedia() { return media; }
    public void setMedia(Map<String, Object> media) { this.media = media; }
    public Map<String, Object> getSocial() { return social; }
    public void setSocial(Map<String, Object> social) { this.social = social; }
    public Map<String, Object> getLive() { return live; }
    public void setLive(Map<String, Object> live) { this.live = live; }
    public Map<String, Object> getRegistration() { return registration; }
    public void setRegistration(Map<String, Object> registration) { this.registration = registration; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
