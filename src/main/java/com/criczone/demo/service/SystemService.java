package com.criczone.demo.service;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.repo.UserRepository;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class SystemService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public SystemService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

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

    @Cacheable(CacheNames.SYSTEM_VERSION)
    public Map<String, Object> version() {
        return Map.of("success", true, "version", "1.0.0-spring");
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
}
