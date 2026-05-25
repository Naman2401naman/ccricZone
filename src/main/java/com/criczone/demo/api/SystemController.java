package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.repo.UserRepository;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public SystemController(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/api/health")
    @Cacheable(CacheNames.SYSTEM_HEALTH)
    public Map<String, Object> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("status", "ok");
        payload.put("timestamp", Instant.now().toString());
        payload.put("uptimeSec", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        payload.put("mongodb", mongoStatus());
        payload.put("users", safeUserCount());
        return payload;
    }

    private String mongoStatus() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return "connected";
        } catch (RuntimeException error) {
            return "disconnected";
        }
    }

    private Long safeUserCount() {
        try {
            return userRepository.count();
        } catch (RuntimeException error) {
            return null;
        }
    }

    @GetMapping("/api/version")
    @Cacheable(CacheNames.SYSTEM_VERSION)
    public Map<String, Object> version() {
        return Map.of("success", true, "version", "1.0.0-spring");
    }
}
