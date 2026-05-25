package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.TeamDocument;
import com.criczone.demo.domain.TournamentDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.TeamRepository;
import com.criczone.demo.repo.TournamentRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;

    public TournamentController(TournamentRepository tournamentRepository, TeamRepository teamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_LIST, key = "#status == null ? 'all' : #status")
    public Map<String, Object> all(@RequestParam(required = false) String status) {
        List<TournamentDocument> tournaments = tournamentRepository.findAll().stream()
            .filter(tournament -> status == null || status.isBlank() || Objects.equals(tournament.getStatus(), status))
            .sorted(Comparator.comparing(TournamentDocument::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
        return Map.of("success", true, "count", tournaments.size(), "tournaments", tournaments);
    }

    @GetMapping("/active")
    @Cacheable(CacheNames.TOURNAMENT_ACTIVE)
    public Map<String, Object> active() {
        List<TournamentDocument> tournaments = tournamentRepository.findAll().stream()
            .filter(tournament -> List.of("registration_open", "ongoing", "playoffs").contains(tournament.getStatus()))
            .collect(Collectors.toList());
        return Map.of("success", true, "count", tournaments.size(), "tournaments", tournaments);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id")
    public Map<String, Object> one(@PathVariable String id) {
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return Map.of("success", true, "tournament", tournament);
    }

    @GetMapping("/{id}/standings")
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id")
    public Map<String, Object> standings(@PathVariable String id) {
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return Map.of("success", true, "standings", tournament.getStandings());
    }

    @GetMapping("/{id}/stats")
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    public Map<String, Object> stats(@PathVariable String id) {
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return Map.of("success", true, "statistics", tournament.getStatistics());
    }

    @PostMapping
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, allEntries = true)
    })
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String name = ApiSupport.trim(request.get("name"));
        Instant startDate = ApiSupport.parseDate(ApiSupport.trim(request.get("startDate")));
        Instant endDate = ApiSupport.parseDate(ApiSupport.trim(request.get("endDate")));
        String venue = ApiSupport.trim(request.get("venue"));
        if (name.isBlank() || startDate == null || endDate == null || venue.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide all required fields (name, startDate, endDate, venue)");
        }

        TournamentDocument tournament = new TournamentDocument();
        tournament.setName(name);
        tournament.setDescription(ApiSupport.trim(request.get("description")));
        tournament.setShortName(Optional.ofNullable(request.get("shortName")).map(String::valueOf).filter(s -> !s.isBlank()).orElse(name.substring(0, Math.min(name.length(), 10)).toUpperCase()));
        tournament.setStartDate(startDate);
        tournament.setEndDate(endDate);
        tournament.setRegistrationDeadline(Optional.ofNullable(ApiSupport.parseDate(ApiSupport.trim(request.get("registrationDeadline")))).orElse(startDate));
        tournament.setVenue(venue);
        tournament.setVenues(List.of(Map.of("name", venue, "location", venue)));
        tournament.setFormat(Optional.ofNullable(request.get("format")).map(String::valueOf).orElse("T20"));
        tournament.setCustomOvers(request.get("customOvers") == null ? null : ApiSupport.safeInt(request.get("customOvers")));
        tournament.setTournamentType(Optional.ofNullable(request.get("tournamentType")).map(String::valueOf).orElse("league_knockout"));
        tournament.setMaxTeams(request.get("maxTeams") == null ? 8 : ApiSupport.safeInt(request.get("maxTeams")));
        tournament.setMinPlayers(request.get("minPlayers") == null ? 11 : ApiSupport.safeInt(request.get("minPlayers")));
        tournament.setMaxPlayers(request.get("maxPlayers") == null ? 15 : ApiSupport.safeInt(request.get("maxPlayers")));
        tournament.setPrizePool(normalizePrizePool(request.get("prizePool")));
        tournament.setPointsSystem(defaultPointsSystem());
        tournament.setStatus("registration_open");
        tournament.setCreatedBy(user.getId());
        tournament.setCreatedAt(Instant.now());
        tournament.setUpdatedAt(Instant.now());
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Tournament created successfully", "tournament", tournament);
    }

    @PostMapping("/{id}/register")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> register(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id,
                                        @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        String teamId = ApiSupport.trim(request.get("teamId"));
        TeamDocument team = teamRepository.findById(teamId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        if (tournament.getRegisteredTeams().stream().anyMatch(entry -> Objects.equals(String.valueOf(entry.get("teamId")), teamId))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Team is already registered");
        }
        if (tournament.getRegisteredTeams().size() >= Optional.ofNullable(tournament.getMaxTeams()).orElse(8)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tournament is full");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> players = (List<Map<String, Object>>) request.getOrDefault("players", List.of());
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("teamId", team.getId());
        entry.put("teamName", Optional.ofNullable(request.get("teamName")).map(String::valueOf).orElse(team.getName()));
        entry.put("captain", request.get("captain"));
        entry.put("viceCaptain", request.get("viceCaptain"));
        entry.put("wicketkeeper", request.get("wicketkeeper"));
        entry.put("coach", request.get("coach"));
        entry.put("players", players);
        entry.put("registeredBy", user.getId());
        entry.put("registeredAt", Instant.now().toString());
        entry.put("stats", Map.of("played", 0, "won", 0, "lost", 0, "tied", 0, "noResult", 0, "points", 0, "netRunRate", 0.0));
        tournament.getRegisteredTeams().add(entry);
        tournament.getStandings().add(ApiSupport.mapOf("teamName", entry.get("teamName"), "teamId", team.getId(), "position", tournament.getStandings().size() + 1, "played", 0, "won", 0, "lost", 0, "tied", 0, "noResult", 0, "points", 0, "netRunRate", 0.0, "form", new ArrayList<>()));
        tournament.setUpdatedAt(Instant.now());
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Team registered successfully", "tournament", tournament);
    }

    @PostMapping("/{id}/unregister")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> unregister(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @PathVariable String id,
                                          @RequestBody Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        String teamId = ApiSupport.trim(request.get("teamId"));
        tournament.setRegisteredTeams(tournament.getRegisteredTeams().stream().filter(entry -> !Objects.equals(String.valueOf(entry.get("teamId")), teamId)).collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        tournament.setStandings(tournament.getStandings().stream().filter(entry -> !Objects.equals(String.valueOf(entry.get("teamId")), teamId)).collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        tournament.setUpdatedAt(Instant.now());
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Team unregistered successfully", "tournament", tournament);
    }

    @PostMapping("/{id}/generate-fixtures")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> generateFixtures(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        List<Map<String, Object>> fixtures = new ArrayList<>();
        List<Map<String, Object>> teams = tournament.getRegisteredTeams();
        int matchNumber = 1;
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                fixtures.add(Map.of(
                    "round", "Match " + matchNumber,
                    "matchNumber", matchNumber,
                    "teamA", teams.get(i).get("teamName"),
                    "teamB", teams.get(j).get("teamName"),
                    "venue", tournament.getVenue(),
                    "status", "scheduled"
                ));
                matchNumber++;
            }
        }
        tournament.setSchedule(fixtures);
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Fixtures generated successfully", "schedule", fixtures);
    }

    @PostMapping("/{id}/generate-playoffs")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> generatePlayoffs(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        List<Map<String, Object>> standings = new ArrayList<>(tournament.getStandings());
        standings.sort(Comparator.comparingDouble(entry -> -ApiSupport.safeDouble(entry.get("points"))));
        List<Map<String, Object>> top4 = standings.stream().limit(4).collect(Collectors.toList());
        tournament.setKnockout(Map.of(
            "qualifier1", top4.size() > 1 ? Map.of("team1", top4.get(0).get("teamName"), "team2", top4.get(1).get("teamName")) : Map.of(),
            "eliminator", top4.size() > 3 ? Map.of("team1", top4.get(2).get("teamName"), "team2", top4.get(3).get("teamName")) : Map.of()
        ));
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Playoffs generated successfully", "knockout", tournament.getKnockout());
    }

    @PostMapping("/standings/update")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, allEntries = true)
    })
    public Map<String, Object> updateStandings(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                               @RequestBody Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(ApiSupport.trim(request.get("tournamentId"))).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standings = (List<Map<String, Object>>) request.get("standings");
        if (standings != null) {
            tournament.setStandings(standings);
            tournamentRepository.save(tournament);
        }
        return Map.of("success", true, "message", "Standings updated successfully", "standings", tournament.getStandings());
    }

    @PutMapping("/{id}/status")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> updateStatus(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                            @PathVariable String id,
                                            @RequestBody Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        tournament.setStatus(ApiSupport.trim(request.get("status")));
        tournament.setUpdatedAt(Instant.now());
        tournamentRepository.save(tournament);
        return Map.of("success", true, "message", "Tournament status updated successfully", "tournament", tournament);
    }

    @DeleteMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_ACTIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, key = "#id")
    })
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        TournamentDocument tournament = tournamentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament not found"));
        if (!Objects.equals(tournament.getCreatedBy(), user.getId()) && !"admin".equalsIgnoreCase(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        tournamentRepository.delete(tournament);
        return Map.of("success", true, "message", "Tournament deleted successfully");
    }

    private Map<String, Object> normalizePrizePool(Object input) {
        if (input instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) input;
            return typed;
        }
        String total = input == null ? "" : String.valueOf(input);
        return Map.of("total", total, "winner", "", "runnerUp", "", "playerOfTournament", "", "currency", "INR");
    }

    private Map<String, Object> defaultPointsSystem() {
        return Map.of("win", 2, "loss", 0, "tie", 1, "noResult", 1, "superOver", true, "bonusPoint", false);
    }
}
