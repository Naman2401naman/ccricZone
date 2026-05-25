package com.criczone.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(AppProperties appProperties) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(cacheNames());
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(appProperties.getCache().getTtlSeconds(), TimeUnit.SECONDS)
            .maximumSize(appProperties.getCache().getMaxEntries()));
        return manager;
    }

    private List<String> cacheNames() {
        return Arrays.asList(
            CacheNames.SYSTEM_HEALTH,
            CacheNames.SYSTEM_VERSION,
            CacheNames.MATCH_LIST,
            CacheNames.MATCH_LIVE,
            CacheNames.MATCH_BY_ID,
            CacheNames.MATCH_HIGHLIGHTS,
            CacheNames.TOURNAMENT_LIST,
            CacheNames.TOURNAMENT_ACTIVE,
            CacheNames.TOURNAMENT_BY_ID,
            CacheNames.TOURNAMENT_STANDINGS,
            CacheNames.TOURNAMENT_STATS,
            CacheNames.TURF_LIST,
            CacheNames.TURF_NEARBY,
            CacheNames.TURF_BY_ID,
            CacheNames.POST_LIST,
            CacheNames.POST_BY_USER,
            CacheNames.PLAYER_PUBLIC,
            CacheNames.PLAYER_SEARCH,
            CacheNames.PLAYER_NEARBY,
            CacheNames.USER_LEADERBOARD,
            CacheNames.GLOBAL_LEADERBOARD);
    }
}
