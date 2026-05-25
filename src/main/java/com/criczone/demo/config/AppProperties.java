package com.criczone.demo.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cache cache = new Cache();
    private Kafka kafka = new Kafka();
    private List<String> clientUrl = new ArrayList<>();
    private boolean allowAllOrigins;

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public List<String> getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(List<String> clientUrl) {
        this.clientUrl = clientUrl;
    }

    public boolean isAllowAllOrigins() {
        return allowAllOrigins;
    }

    public void setAllowAllOrigins(boolean allowAllOrigins) {
        this.allowAllOrigins = allowAllOrigins;
    }

    public static class Jwt {
        private String secret;
        private long expirationMs;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }
    }

    public static class Cache {
        private long ttlSeconds = 60;
        private long maxEntries = 500;

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public long getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(long maxEntries) {
            this.maxEntries = maxEntries;
        }
    }

    public static class Kafka {
        private boolean enabled;
        private String bookingTopic = "criczone.booking.workflow.v1";
        private String consumerGroupId = "criczone-booking-workflow";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBookingTopic() {
            return bookingTopic;
        }

        public void setBookingTopic(String bookingTopic) {
            this.bookingTopic = bookingTopic;
        }

        public String getConsumerGroupId() {
            return consumerGroupId;
        }

        public void setConsumerGroupId(String consumerGroupId) {
            this.consumerGroupId = consumerGroupId;
        }
    }
}
