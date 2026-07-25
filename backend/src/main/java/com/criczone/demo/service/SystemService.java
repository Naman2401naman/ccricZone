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

    public Map<String, Object> health() {
        boolean mongoConnected = isMongoConnected();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", mongoConnected);
        payload.put("status", mongoConnected ? "ok" : "degraded");
        payload.put("timestamp", Instant.now().toString());
        payload.put("uptimeSec", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        payload.put("mongodb", mongoConnected ? "connected" : "disconnected");
        payload.put("users", safeUserCount());
        return payload;
    }

    @Cacheable(CacheNames.SYSTEM_VERSION)
    public Map<String, Object> version() {
        return Map.of("success", true, "version", "1.0.0-spring");
    }

    private boolean isMongoConnected() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return true;
        } catch (RuntimeException error) {
            return false;
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
