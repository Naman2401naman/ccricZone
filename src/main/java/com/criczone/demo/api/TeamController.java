package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CreateTeamRequest;
import com.criczone.demo.dto.ApiRequests.RandomizeTeamsRequest;
import com.criczone.demo.dto.ApiRequests.RespondInvitationRequest;
import com.criczone.demo.dto.ApiRequests.UpdateTeamRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Reusable teams, invitations, suggestions, and randomization")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public Map<String, Object> createTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @Valid @RequestBody CreateTeamRequest request) {
        return teamService.createTeam(currentUser, RequestMaps.toMap(request));
    }

    @GetMapping
    public Map<String, Object> getUserTeams(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return teamService.getUserTeams(currentUser);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String id) {
        return teamService.getTeam(currentUser, id);
    }

    @GetMapping("/{id}/players")
    public Map<String, Object> getTeamPlayers(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                              @PathVariable String id) {
        return teamService.getTeamPlayers(currentUser, id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @PathVariable String id,
                                          @Valid @RequestBody UpdateTeamRequest request) {
        return teamService.updateTeam(currentUser, id, RequestMaps.toMap(request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTeam(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @PathVariable String id) {
        return teamService.deleteTeam(currentUser, id);
    }

    @GetMapping("/suggestions")
    public Map<String, Object> suggestions(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @RequestParam(required = false, defaultValue = "") String q,
                                           @RequestParam(required = false, defaultValue = "12") int limit) {
        return teamService.suggestions(currentUser, q, limit);
    }

    @GetMapping("/invitations/my")
    public Map<String, Object> myInvitations(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return teamService.myInvitations(currentUser);
    }

    @PutMapping("/{id}/invitations/{memberId}/respond")
    public Map<String, Object> respond(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String id,
                                       @PathVariable String memberId,
                                       @Valid @RequestBody RespondInvitationRequest request) {
        return teamService.respond(currentUser, id, memberId, RequestMaps.toMap(request));
    }

    @PostMapping("/randomize")
    public Map<String, Object> randomize(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                         @Valid @RequestBody RandomizeTeamsRequest request) {
        return teamService.randomize(currentUser, RequestMaps.toMap(request));
    }
}
