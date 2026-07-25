package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.TeamSegregationRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.TeamSegregationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team-segregation")
@Tag(name = "Team Segregation", description = "Team grouping by past turf performance")
public class TeamSegregationController {

    private final TeamSegregationService teamSegregationService;

    public TeamSegregationController(TeamSegregationService teamSegregationService) {
        this.teamSegregationService = teamSegregationService;
    }

    @PostMapping
    public Map<String, Object> segregate(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                         @Valid @RequestBody TeamSegregationRequest request) {
        return teamSegregationService.segregate(currentUser, RequestMaps.toMap(request));
    }

    @PostMapping("/train")
    public Map<String, Object> train(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                     @Valid @RequestBody TeamSegregationRequest request) {
        return teamSegregationService.train(currentUser, RequestMaps.toMap(request));
    }

    @GetMapping("/performance")
    public Map<String, Object> performance(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                           @RequestParam(required = false, defaultValue = "") String venue) {
        return teamSegregationService.performance(currentUser, venue);
    }
}
