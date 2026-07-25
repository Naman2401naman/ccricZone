package com.criczone.demo.config;

import com.mongodb.MongoClientSettings;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final int TIMEOUT_MS = 3_000;

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoTimeoutCustomizer() {
        return (MongoClientSettings.Builder builder) -> builder
            .applyToClusterSettings(settings -> settings.serverSelectionTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS))
            .applyToSocketSettings(settings -> settings.connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
}
