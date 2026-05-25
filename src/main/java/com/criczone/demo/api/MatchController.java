package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.MatchDocument;
import com.criczone.demo.domain.TeamDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.MatchRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public MatchController(MatchRepository matchRepository, TeamRepository teamRepository, TournamentRepository tournamentRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @GetMapping
    @Cacheable(CacheNames.MATCH_LIST)
    public Map<String, Object> allMatches() {
        List<MatchDocument> matches = matchRepository.findAll().stream()
            .sorted(Comparator.comparing(MatchDocument::getMatchDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
        return Map.of("success", true, "count", matches.size(), "data", matches);
    }

    @GetMapping("/live")
    @Cacheable(CacheNames.MATCH_LIVE)
    public Map<String, Object> liveMatches() {
        List<MatchDocument> matches = matchRepository.findAll().stream().filter(match -> "live".equalsIgnoreCase(match.getStatus())).collect(Collectors.toList());
        return Map.of("success", true, "count", matches.size(), "data", matches);
    }

    @GetMapping("/user/my-matches")
    public Map<String, Object> myMatches(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        List<MatchDocument> matches = matchRepository.findAll().stream()
            .filter(match -> Objects.equals(match.getCreatedBy(), user.getId())
                || hasPlayer(match.getTeamA(), user.getId())
                || hasPlayer(match.getTeamB(), user.getId()))
            .sorted(Comparator.comparing(MatchDocument::getMatchDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
        return Map.of("success", true, "count", matches.size(), "data", matches);
    }

    @PostMapping
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.MATCH_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_LIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_BY_ID, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_HIGHLIGHTS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_BY_ID, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STANDINGS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TOURNAMENT_STATS, allEntries = true)
    })
    public Map<String, Object> createMatch(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String matchName = ApiSupport.trim(request.get("matchName"));
        String matchType = Optional.ofNullable(request.get("matchType")).map(String::valueOf).orElse("T20");
        String venue = ApiSupport.trim(request.get("venue"));
        Instant matchDate = ApiSupport.parseDate(ApiSupport.trim(request.get("matchDate")));
        if (matchName.isBlank() || venue.isBlank() || matchDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide all required fields");
        }

        int totalOvers;
        if ("ODI".equals(matchType)) {
            totalOvers = 50;
        } else if ("Test".equals(matchType)) {
            totalOvers = 90;
        } else if ("Custom".equals(matchType)) {
            totalOvers = Math.max(1, Math.min(50, ApiSupport.safeInt(request.get("customOvers"))));
        } else {
            totalOvers = 20;
        }

        MatchDocument match = new MatchDocument();
        match.setMatchName(matchName);
        match.setMatchType(matchType);
        match.setTotalOvers(totalOvers);
        match.setVenue(venue);
        match.setMatchDate(matchDate);
        match.setCreatedBy(user.getId());
        match.setTournament(ApiSupport.trim(request.get("tournamentId")).isBlank() ? null : ApiSupport.trim(request.get("tournamentId")));
        match.setTeamA(resolveTeamPayload(ApiSupport.trim(request.get("teamAName")), ApiSupport.trim(request.get("teamAId")), request.get("teamAPlayers")));
        match.setTeamB(resolveTeamPayload(ApiSupport.trim(request.get("teamBName")), ApiSupport.trim(request.get("teamBId")), request.get("teamBPlayers")));
        match.setInnings(initialInnings(match));
        match.setCreatedAt(Instant.now());
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);

        if (match.getTournament() != null && tournamentRepository.findById(match.getTournament()).isPresent()) {
            com.criczone.demo.domain.TournamentDocument tournament = tournamentRepository.findById(match.getTournament()).orElseThrow();
            List<String> ids = new ArrayList<>(tournament.getMatches());
            ids.add(match.getId());
            tournament.setMatches(ids);
            tournamentRepository.save(tournament);
        }

        return Map.of("success", true, "message", "Match created successfully", "data", match);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = CacheNames.MATCH_BY_ID, key = "#id")
    public Map<String, Object> getMatch(@PathVariable String id) {
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        return Map.of("success", true, "data", match);
    }

    @PutMapping("/{id}/toss")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.MATCH_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_LIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.MATCH_HIGHLIGHTS, key = "#id")
    })
    public Map<String, Object> setToss(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String id,
                                       @RequestBody Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        String tossWinnerTeam = ApiSupport.trim(request.get("tossWinnerTeam"));
        String decision = ApiSupport.trim(request.get("decision"));
        if (!List.of("teamA", "teamB").contains(tossWinnerTeam)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid toss winner team");
        }
        match.setToss(Map.of("winner", tossWinnerTeam, "decision", decision));
        String battingTeam = "bat".equalsIgnoreCase(decision) ? tossWinnerTeam : ("teamA".equals(tossWinnerTeam) ? "teamB" : "teamA");
        String bowlingTeam = "teamA".equals(battingTeam) ? "teamB" : "teamA";
        match.setStatus("live");
        match.setCurrentInning(1);
        match.setInnings(ApiSupport.mapOf(
            "first", ApiSupport.mapOf("battingTeam", battingTeam, "bowlingTeam", bowlingTeam, "score", 0, "wickets", 0, "overs", 0, "balls", 0, "isCompleted", false, "extras", defaultExtras(), "target", null),
            "second", ApiSupport.mapOf("battingTeam", bowlingTeam, "bowlingTeam", battingTeam, "score", 0, "wickets", 0, "overs", 0, "balls", 0, "isCompleted", false, "extras", defaultExtras(), "target", null)
        ));
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);
        return Map.of("success", true, "message", "Toss updated successfully", "data", match);
    }

    @PutMapping("/{id}/score")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.MATCH_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_LIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.MATCH_HIGHLIGHTS, key = "#id")
    })
    public Map<String, Object> updateScore(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @PathVariable String id,
                                           @RequestBody Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        int inning = Optional.ofNullable(match.getCurrentInning()).orElse(1);
        String inningKey = inning == 1 ? "first" : "second";
        @SuppressWarnings("unchecked")
        Map<String, Object> inningMap = (Map<String, Object>) match.getInnings().get(inningKey);
        if (inningMap == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Innings state is not initialized");
        }
        String battingKey = String.valueOf(inningMap.get("battingTeam"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ballEvents = (List<Map<String, Object>>) request.getOrDefault("ballEvents", List.of());

        int runs = ApiSupport.safeInt(request.get("runs"));
        int wickets = ApiSupport.safeInt(request.get("wickets"));
        int balls = ApiSupport.oversToBalls(ApiSupport.trim(request.get("overs")));
        inningMap.put("score", runs);
        inningMap.put("wickets", wickets);
        inningMap.put("overs", balls / 6);
        inningMap.put("balls", balls % 6);

        Map<String, Object> battingTeam = "teamB".equals(battingKey) ? match.getTeamB() : match.getTeamA();
        battingTeam.put("score", runs);
        battingTeam.put("wickets", wickets);
        battingTeam.put("overs", ApiSupport.trim(request.get("overs")));
        battingTeam.put("ballsPlayed", balls);

        match.setCurrentStriker(ApiSupport.trim(request.get("batsmanName")));
        match.setCurrentStrikerId(ApiSupport.trim(request.get("batsmanId")));
        match.setCurrentNonStriker(ApiSupport.trim(request.get("nonStrikerName")));
        match.setCurrentNonStrikerId(ApiSupport.trim(request.get("nonStrikerId")));
        match.setCurrentBowler(ApiSupport.trim(request.get("bowlerName")));
        match.setCurrentBowlerId(ApiSupport.trim(request.get("bowlerId")));
        match.setCurrentOver(ApiSupport.mapOf("overNumber", balls / 6, "ballNumber", balls % 6));
        match.setBallByBallData(buildBallData(ballEvents, inning, match));
        match.setBatsmanStats(buildBatsmanStats(ballEvents, inning));
        match.setBowlerStats(buildBowlerStats(ballEvents, inning));
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);
        return Map.of("success", true, "message", "Score updated successfully", "data", match);
    }

    @PutMapping("/{id}/complete")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.MATCH_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_LIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.MATCH_HIGHLIGHTS, key = "#id")
    })
    public Map<String, Object> complete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id,
                                        @RequestBody(required = false) Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        match.setStatus("completed");
        if (request != null) {
            match.setWinner(ApiSupport.trim(request.get("winner")));
            if (!ApiSupport.trim(request.get("resultType")).isBlank()) {
                match.setResultType(ApiSupport.trim(request.get("resultType")));
            }
            if (request.get("resultMargin") != null) {
                match.setResultMargin(ApiSupport.safeInt(request.get("resultMargin")));
            }
        }
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);
        return Map.of("success", true, "message", "Match completed successfully", "data", match);
    }

    @DeleteMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.MATCH_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_LIVE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.MATCH_BY_ID, key = "#id"),
        @CacheEvict(cacheNames = CacheNames.MATCH_HIGHLIGHTS, key = "#id")
    })
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        if (!Objects.equals(match.getCreatedBy(), user.getId()) && !"admin".equalsIgnoreCase(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        matchRepository.delete(match);
        return Map.of("success", true, "message", "Match deleted successfully");
    }

    @GetMapping("/{id}/highlights")
    @Cacheable(cacheNames = CacheNames.MATCH_HIGHLIGHTS, key = "#id")
    public Map<String, Object> highlights(@PathVariable String id) {
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        List<Map<String, Object>> highlights = match.getBallByBallData().stream()
            .filter(ball -> Boolean.TRUE.equals(ball.get("isWicket")) || ApiSupport.safeInt(ball.get("runs")) >= 4)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", highlights.size(), "highlights", highlights);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<String> report(@PathVariable String id, @RequestParam(defaultValue = "json") String format) {
        MatchDocument match = matchRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"success\":true}");
        }
        String csv = "Match,Venue,Date,Status,Winner\n" +
            csv(match.getMatchName()) + "," + csv(match.getVenue()) + "," + csv(match.getMatchDate()) + "," + csv(match.getStatus()) + "," + csv(match.getWinner()) + "\n";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"match-report-" + id + ".csv\"")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv);
    }

    private Map<String, Object> resolveTeamPayload(String requestedName, String teamId, Object rawPlayers) {
        TeamDocument teamDoc = teamId == null || teamId.isBlank() ? null : teamRepository.findById(teamId).orElse(null);
        String name = !requestedName.isBlank() ? requestedName : (teamDoc != null ? teamDoc.getName() : "");
        if (name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Both team names are required");
        }
        List<Map<String, Object>> playerLinks = new ArrayList<>();
        if (rawPlayers instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> players = (List<Map<String, Object>>) rawPlayers;
            for (Map<String, Object> player : ApiSupport.normalizeMembers(players)) {
                playerLinks.add(ApiSupport.mapOf("name", player.get("name"), "email", player.get("email"), "userId", player.get("userId"), "isRegistered", player.get("userId") != null));
            }
        }
        if (playerLinks.isEmpty() && teamDoc != null) {
            playerLinks = teamDoc.getMembers().stream()
                .filter(member -> !"rejected".equalsIgnoreCase(String.valueOf(member.get("inviteStatus"))))
                .map(member -> ApiSupport.mapOf(
                    "name", member.get("name"),
                    "email", member.get("email"),
                    "userId", member.get("player"),
                    "isRegistered", member.get("player") != null))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return ApiSupport.mapOf("name", name, "teamId", teamId.isBlank() ? null : teamId, "players", playerLinks.stream().map(item -> String.valueOf(item.get("name"))).collect(Collectors.toList()), "playerLinks", playerLinks, "score", 0, "wickets", 0, "overs", "0.0", "ballsPlayed", 0);
    }

    private Map<String, Object> initialInnings(MatchDocument match) {
        return ApiSupport.mapOf(
            "first", ApiSupport.mapOf("battingTeam", "teamA", "bowlingTeam", "teamB", "score", 0, "wickets", 0, "overs", 0, "balls", 0, "isCompleted", false, "extras", defaultExtras(), "target", null),
            "second", ApiSupport.mapOf("battingTeam", "teamB", "bowlingTeam", "teamA", "score", 0, "wickets", 0, "overs", 0, "balls", 0, "isCompleted", false, "extras", defaultExtras(), "target", null)
        );
    }

    private Map<String, Object> defaultExtras() {
        return ApiSupport.mapOf("total", 0, "wides", 0, "noBalls", 0, "byes", 0, "legByes", 0, "penalties", 0);
    }

    private boolean hasPlayer(Map<String, Object> team, String userId) {
        Object links = team.get("playerLinks");
        if (!(links instanceof List<?>)) return false;
        List<?> list = (List<?>) links;
        return list.stream().filter(Map.class::isInstance).map(Map.class::cast).anyMatch(player -> Objects.equals(String.valueOf(player.get("userId")), userId));
    }

    private List<Map<String, Object>> buildBallData(List<Map<String, Object>> events, int inning, MatchDocument match) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Map<String, Object> event = events.get(i);
            boolean isExtra = Boolean.TRUE.equals(event.get("isExtra"));
            int ballIndex = i;
            int runs = ApiSupport.safeInt(event.get("runs"));
            String extraType = Optional.ofNullable(event.get("extraType")).map(String::valueOf).orElse("none");
            data.add(ApiSupport.mapOf(
                "ballNumber", ballIndex,
                "inning", inning,
                "over", ballIndex / 6,
                "ballInOver", ballIndex % 6,
                "isLegalDelivery", !isExtra,
                "batsmanName", event.get("strikerName"),
                "batsmanId", event.get("strikerId"),
                "nonStrikerName", event.get("nonStrikerName"),
                "bowlerName", event.get("bowlerName"),
                "bowlerId", event.get("bowlerId"),
                "runs", runs,
                "totalRuns", runs,
                "batsmanRuns", isExtra ? 0 : runs,
                "extras", ApiSupport.mapOf("total", isExtra ? runs : 0, "type", isExtra ? normalizeExtraType(extraType) : "none", "runs", isExtra ? runs : 0),
                "isWicket", Boolean.TRUE.equals(event.get("isWicket")),
                "wicket", ApiSupport.mapOf("playerOutName", event.get("wicketPlayerName"), "playerOutId", event.get("wicketPlayerId"), "kind", event.get("wicketKind")),
                "commentary", event.get("commentary"),
                "timestamp", Instant.now().toString()
            ));
        }
        return data;
    }

    private String normalizeExtraType(String extraType) {
        String normalized = extraType == null ? "" : extraType.toLowerCase();
        if ("noball".equals(normalized)) return "noball";
        if ("wide".equals(normalized)) return "wide";
        if ("bye".equals(normalized)) return "bye";
        if ("legbye".equals(normalized)) return "legbye";
        return "none";
    }

    private List<Map<String, Object>> buildBatsmanStats(List<Map<String, Object>> events, int inning) {
        Map<String, Map<String, Object>> stats = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            String name = ApiSupport.trim(event.get("strikerName"));
            if (name.isBlank()) continue;
            Map<String, Object> row = stats.computeIfAbsent(name, key -> new LinkedHashMap<>(ApiSupport.mapOf(
                "name", key, "inning", inning, "runs", 0, "ballsFaced", 0, "fours", 0, "sixes", 0, "strikeRate", 0, "isOut", false
            )));
            int runs = ApiSupport.safeInt(event.get("runs"));
            boolean extra = Boolean.TRUE.equals(event.get("isExtra"));
            if (!extra) {
                row.put("runs", ApiSupport.safeInt(row.get("runs")) + runs);
                row.put("ballsFaced", ApiSupport.safeInt(row.get("ballsFaced")) + 1);
                if (runs == 4) row.put("fours", ApiSupport.safeInt(row.get("fours")) + 1);
                if (runs == 6) row.put("sixes", ApiSupport.safeInt(row.get("sixes")) + 1);
            }
            if (Boolean.TRUE.equals(event.get("isWicket"))) {
                row.put("isOut", true);
                row.put("dismissal", ApiSupport.mapOf("kind", event.get("wicketKind"), "overNumber", stats.size()));
            }
            int balls = Math.max(1, ApiSupport.safeInt(row.get("ballsFaced")));
            row.put("strikeRate", Math.round((ApiSupport.safeDouble(row.get("runs")) / balls) * 10000d) / 100d);
        }
        return new ArrayList<>(stats.values());
    }

    private List<Map<String, Object>> buildBowlerStats(List<Map<String, Object>> events, int inning) {
        Map<String, Map<String, Object>> stats = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            String name = ApiSupport.trim(event.get("bowlerName"));
            if (name.isBlank()) continue;
            Map<String, Object> row = stats.computeIfAbsent(name, key -> new LinkedHashMap<>(ApiSupport.mapOf(
                "name", key, "inning", inning, "overs", 0, "balls", 0, "maidens", 0, "runs", 0, "wickets", 0, "economy", 0
            )));
            row.put("runs", ApiSupport.safeInt(row.get("runs")) + ApiSupport.safeInt(event.get("runs")));
            if (!Boolean.TRUE.equals(event.get("isExtra"))) {
                row.put("balls", ApiSupport.safeInt(row.get("balls")) + 1);
            }
            if (Boolean.TRUE.equals(event.get("isWicket"))) {
                row.put("wickets", ApiSupport.safeInt(row.get("wickets")) + 1);
            }
            int balls = ApiSupport.safeInt(row.get("balls"));
            row.put("overs", ApiSupport.ballsToOvers(balls));
            double oversDecimal = balls == 0 ? 1 : balls / 6.0;
            row.put("economy", Math.round((ApiSupport.safeDouble(row.get("runs")) / oversDecimal) * 100d) / 100d);
        }
        return new ArrayList<>(stats.values());
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.contains(",") || text.contains("\"")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
