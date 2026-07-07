package com.criczone.demo.service;

import com.criczone.demo.domain.TeamDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.TeamRepository;
import com.criczone.demo.repo.UserRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.stereotype.Service;
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Map<String, Object> createTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String name = ApiSupport.trim(request.get("name"));
        if (name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Team name is required");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawMembers = (List<Map<String, Object>>) request.getOrDefault("members", List.of());
        List<Map<String, Object>> members = normalizeMembers(rawMembers, user);
        if (members.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide at least one team member");
        }

        TeamDocument team = new TeamDocument();
        team.setName(name);
        team.setOwner(user.getId());
        team.setMembers(members);
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());
        teamRepository.save(team);

        user.getTeams().add(Map.of("teamId", team.getId(), "teamName", team.getName(), "joinedAt", Instant.now().toString(), "isActive", true));
        userRepository.save(user);

        return Map.of("success", true, "message", "Team created successfully", "data", team);
    }

    @GetMapping
    public Map<String, Object> getUserTeams(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        List<TeamDocument> teams = teamRepository.findAll().stream()
            .filter(team -> Objects.equals(team.getOwner(), user.getId()) || team.getMembers().stream().anyMatch(member -> Objects.equals(String.valueOf(member.get("player")), user.getId())))
            .sorted(Comparator.comparing(TeamDocument::getUpdatedAt).reversed())
            .collect(Collectors.toList());
        return Map.of("success", true, "count", teams.size(), "data", teams);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        ApiSupport.requireUser(currentUser);
        TeamDocument team = teamRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        return Map.of("success", true, "data", team);
    }

    @GetMapping("/{id}/players")
    public Map<String, Object> getTeamPlayers(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        ApiSupport.requireUser(currentUser);
        TeamDocument team = teamRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        return Map.of("success", true, "count", team.getMembers().size(), "data", team.getMembers());
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @PathVariable String id,
                                          Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        TeamDocument team = teamRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        ensureOwner(user, team);

        if (request.get("name") != null) {
            team.setName(ApiSupport.trim(request.get("name")));
        }
        if (request.get("members") instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawMembers = (List<Map<String, Object>>) request.get("members");
            team.setMembers(normalizeMembers(rawMembers, user));
        }
        team.setUpdatedAt(Instant.now());
        teamRepository.save(team);
        return Map.of("success", true, "message", "Team updated successfully", "data", team);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        TeamDocument team = teamRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        ensureOwner(user, team);
        teamRepository.delete(team);
        return Map.of("success", true, "message", "Team deleted successfully");
    }

    @GetMapping("/suggestions")
    public Map<String, Object> suggestions(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @RequestParam(required = false, defaultValue = "") String q,
                                           @RequestParam(required = false, defaultValue = "12") int limit) {
        ApiSupport.requireUser(currentUser);
        String probe = q.toLowerCase();
        List<Map<String, Object>> data = userRepository.findAll().stream()
            .filter(user -> probe.isBlank() || user.getName().toLowerCase().contains(probe) || user.getEmail().toLowerCase().contains(probe))
            .sorted(Comparator.comparing(UserDocument::getName))
            .limit(limit)
            .map(user -> Map.of(
                "_id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "profile", user.getProfile(),
                "stats", user.getStats(),
                "rankings", user.getRankings()))
            .collect(Collectors.toList());
        return Map.of("success", true, "count", data.size(), "data", data);
    }

    @GetMapping("/invitations/my")
    public Map<String, Object> myInvitations(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        List<Map<String, Object>> data = new ArrayList<>();
        for (TeamDocument team : teamRepository.findAll()) {
            for (Map<String, Object> member : team.getMembers()) {
                if (Objects.equals(String.valueOf(member.get("player")), user.getId()) && "pending".equalsIgnoreCase(String.valueOf(member.get("inviteStatus")))) {
                    Map<String, Object> invite = new LinkedHashMap<>(member);
                    invite.put("teamId", team.getId());
                    invite.put("teamName", team.getName());
                    invite.put("memberId", member.get("memberId"));
                    data.add(invite);
                }
            }
        }
        return Map.of("success", true, "count", data.size(), "data", data);
    }

    @PutMapping("/{id}/invitations/{memberId}/respond")
    public Map<String, Object> respond(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String id,
                                       @PathVariable String memberId,
                                       Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        TeamDocument team = teamRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        String action = ApiSupport.trim(request.get("action"));
        Map<String, Object> member = team.getMembers().stream()
            .filter(item -> Objects.equals(String.valueOf(item.get("memberId")), memberId))
            .findFirst()
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (!Objects.equals(String.valueOf(member.get("player")), user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to respond to this invitation");
        }
        member.put("inviteStatus", "accept".equalsIgnoreCase(action) ? "accepted" : "rejected");
        member.put("respondedAt", Instant.now().toString());
        team.setUpdatedAt(Instant.now());
        teamRepository.save(team);
        return Map.of("success", true, "message", "Invitation response saved", "data", team);
    }

    @PostMapping("/randomize")
    public Map<String, Object> randomize(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                         Map<String, Object> request) {
        ApiSupport.requireUser(currentUser);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> players = ApiSupport.normalizeMembers((List<Map<String, Object>>) request.getOrDefault("players", List.of()));
        if (players.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least two players are required for random team distribution");
        }
        Collections.shuffle(players);
        List<Map<String, Object>> teamAPlayers = new ArrayList<>();
        List<Map<String, Object>> teamBPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i % 2 == 0) teamAPlayers.add(players.get(i)); else teamBPlayers.add(players.get(i));
        }
        return Map.of(
            "success", true,
            "distribution", Map.of(
                "teamA", Map.of("name", Optional.ofNullable(request.get("teamAName")).map(String::valueOf).orElse("Team A"), "players", teamAPlayers),
                "teamB", Map.of("name", Optional.ofNullable(request.get("teamBName")).map(String::valueOf).orElse("Team B"), "players", teamBPlayers)
            )
        );
    }

    private List<Map<String, Object>> normalizeMembers(List<Map<String, Object>> rawMembers, UserDocument owner) {
        List<Map<String, Object>> members = ApiSupport.normalizeMembers(rawMembers);
        List<Map<String, Object>> finalMembers = new ArrayList<>();
        boolean ownerIncluded = false;
        for (Map<String, Object> member : members) {
            String userId = Optional.ofNullable(member.get("userId")).map(String::valueOf).orElse("");
            Optional<UserDocument> linked = userId.isBlank()
                ? userRepository.findByEmail(Optional.ofNullable(member.get("email")).map(String::valueOf).orElse("").toLowerCase())
                : userRepository.findById(userId);
            Map<String, Object> normalized = new LinkedHashMap<>();
            UserDocument found = linked.orElse(null);
            normalized.put("memberId", java.util.UUID.randomUUID().toString());
            normalized.put("player", found != null ? found.getId() : null);
            normalized.put("name", found != null ? found.getName() : member.get("name"));
            normalized.put("email", found != null ? found.getEmail() : member.get("email"));
            normalized.put("isRegistered", found != null);
            boolean isOwner = found != null && Objects.equals(found.getId(), owner.getId());
            ownerIncluded = ownerIncluded || isOwner;
            normalized.put("inviteStatus", (found != null && !isOwner) ? "pending" : "accepted");
            normalized.put("invitedBy", isOwner ? null : owner.getId());
            normalized.put("respondedAt", isOwner ? Instant.now().toString() : null);
            normalized.put("addedAt", Instant.now().toString());
            finalMembers.add(normalized);
        }
        if (!ownerIncluded) {
            finalMembers.add(0, ApiSupport.mapOf(
                "memberId", java.util.UUID.randomUUID().toString(),
                "player", owner.getId(),
                "name", owner.getName(),
                "email", owner.getEmail(),
                "isRegistered", true,
                "inviteStatus", "accepted",
                "invitedBy", null,
                "respondedAt", Instant.now().toString(),
                "addedAt", Instant.now().toString()
            ));
        }
        return finalMembers.stream().collect(Collectors.toCollection(ArrayList::new));
    }

    private void ensureOwner(UserDocument user, TeamDocument team) {
        if (!Objects.equals(team.getOwner(), user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
    }
}



