package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CreateTournamentRequest;
import com.criczone.demo.dto.ApiRequests.TournamentTeamRequest;
import com.criczone.demo.dto.ApiRequests.UpdateStandingsRequest;
import com.criczone.demo.dto.ApiRequests.UpdateStatusRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.TournamentService;
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
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public Map<String, Object> all(@RequestParam(required = false) String status) {
        return tournamentService.all(status);
    }

    @GetMapping("/active")
    public Map<String, Object> active() {
        return tournamentService.active();
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(@PathVariable String id) {
        return tournamentService.one(id);
    }

    @GetMapping("/{id}/standings")
    public Map<String, Object> standings(@PathVariable String id) {
        return tournamentService.standings(id);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> stats(@PathVariable String id) {
        return tournamentService.stats(id);
    }

    @PostMapping
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @Valid @RequestBody CreateTournamentRequest request) {
        return tournamentService.create(currentUser, RequestMaps.toMap(request));
    }

    @PostMapping("/{id}/register")
    public Map<String, Object> register(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id,
                                        @Valid @RequestBody TournamentTeamRequest request) {
        return tournamentService.register(currentUser, id, RequestMaps.toMap(request));
    }

    @PostMapping("/{id}/unregister")
    public Map<String, Object> unregister(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @PathVariable String id,
                                          @Valid @RequestBody TournamentTeamRequest request) {
        return tournamentService.unregister(currentUser, id, RequestMaps.toMap(request));
    }

    @PostMapping("/{id}/generate-fixtures")
    public Map<String, Object> generateFixtures(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                                @PathVariable String id) {
        return tournamentService.generateFixtures(currentUser, id);
    }

    @PostMapping("/{id}/generate-playoffs")
    public Map<String, Object> generatePlayoffs(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                                @PathVariable String id) {
        return tournamentService.generatePlayoffs(currentUser, id);
    }

    @PostMapping("/standings/update")
    public Map<String, Object> updateStandings(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                               @Valid @RequestBody UpdateStandingsRequest request) {
        return tournamentService.updateStandings(currentUser, RequestMaps.toMap(request));
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                            @PathVariable String id,
                                            @Valid @RequestBody UpdateStatusRequest request) {
        return tournamentService.updateStatus(currentUser, id, RequestMaps.toMap(request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id) {
        return tournamentService.delete(currentUser, id);
    }
}
