package com.criczone.demo.api;

import com.criczone.demo.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
@Tag(name = "Leaderboard", description = "Global cricket rankings by batting, bowling, and all-round metrics")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/batsmen")
    public Map<String, Object> batsmen(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.batsmen(limit);
    }

    @GetMapping("/bowlers")
    public Map<String, Object> bowlers(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.bowlers(limit);
    }

    @GetMapping("/all-rounders")
    public Map<String, Object> allRounders(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.allRounders(limit);
    }
}
