package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CompleteMatchRequest;
import com.criczone.demo.dto.ApiRequests.CreateMatchRequest;
import com.criczone.demo.dto.ApiRequests.ScoreRequest;
import com.criczone.demo.dto.ApiRequests.TossRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.MatchService;
import java.util.Map;
import javax.validation.Valid;
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

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public Map<String, Object> allMatches() {
        return matchService.allMatches();
    }

    @GetMapping("/live")
    public Map<String, Object> liveMatches() {
        return matchService.liveMatches();
    }

    @GetMapping("/user/my-matches")
    public Map<String, Object> myMatches(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return matchService.myMatches(currentUser);
    }

    @PostMapping
    public Map<String, Object> createMatch(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @Valid @RequestBody CreateMatchRequest request) {
        return matchService.createMatch(currentUser, RequestMaps.toMap(request));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMatch(@PathVariable String id) {
        return matchService.getMatch(id);
    }

    @PutMapping("/{id}/toss")
    public Map<String, Object> setToss(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String id,
                                       @Valid @RequestBody TossRequest request) {
        return matchService.setToss(currentUser, id, RequestMaps.toMap(request));
    }

    @PutMapping("/{id}/score")
    public Map<String, Object> updateScore(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @PathVariable String id,
                                           @Valid @RequestBody ScoreRequest request) {
        return matchService.updateScore(currentUser, id, RequestMaps.toMap(request));
    }

    @PutMapping({"/{id}/complete", "/{id}/innings/complete"})
    public Map<String, Object> complete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id,
                                        @RequestBody(required = false) CompleteMatchRequest request) {
        return matchService.complete(currentUser, id, request == null ? null : RequestMaps.toMap(request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id) {
        return matchService.delete(currentUser, id);
    }

    @GetMapping("/{id}/highlights")
    public Map<String, Object> highlights(@PathVariable String id) {
        return matchService.highlights(id);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<String> report(@PathVariable String id, @RequestParam(defaultValue = "json") String format) {
        return matchService.report(id, format);
    }
}
