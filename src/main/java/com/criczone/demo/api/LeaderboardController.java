package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.UserRepository;
import com.criczone.demo.support.ApiSupport;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/batsmen")
    @Cacheable(cacheNames = CacheNames.GLOBAL_LEADERBOARD, key = "'batsmen:' + #limit")
    public Map<String, Object> batsmen(@RequestParam(defaultValue = "10") int limit) {
        return Map.of("success", true, "count", limit, "leaderboard", top(limit, "batting", "runs"));
    }

    @GetMapping("/bowlers")
    @Cacheable(cacheNames = CacheNames.GLOBAL_LEADERBOARD, key = "'bowlers:' + #limit")
    public Map<String, Object> bowlers(@RequestParam(defaultValue = "10") int limit) {
        return Map.of("success", true, "count", limit, "leaderboard", top(limit, "bowling", "wickets"));
    }

    @GetMapping("/all-rounders")
    @Cacheable(cacheNames = CacheNames.GLOBAL_LEADERBOARD, key = "'all-rounders:' + #limit")
    public Map<String, Object> allRounders(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = userRepository.findAll().stream()
            .filter(user -> "All-rounder".equalsIgnoreCase(String.valueOf(user.getProfile().getOrDefault("playerType", ""))))
            .sorted(Comparator.comparingDouble(user -> -ApiSupport.safeDouble(user.getRankings().get("allRounder"))))
            .limit(limit)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", data.size(), "leaderboard", data);
    }

    private List<Map<String, Object>> top(int limit, String section, String metric) {
        return userRepository.findAll().stream()
            .sorted(Comparator.comparingDouble(user -> -metricValue(user, section, metric)))
            .limit(limit)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
    }

    private double metricValue(UserDocument user, String section, String metric) {
        Object statsSection = user.getStats().get(section);
        if (statsSection instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) statsSection;
            return ApiSupport.safeDouble(map.get(metric));
        }
        return 0;
    }
}
