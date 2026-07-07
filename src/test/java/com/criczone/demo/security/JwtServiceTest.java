package com.criczone.demo.security;

import com.criczone.demo.config.AppProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void rejectsPlaceholderSecret() {
        JwtService service = new JwtService(properties("change-me-change-me-change-me-change-me"));

        assertThrows(IllegalStateException.class, service::validateSigningKey);
    }

    @Test
    void rejectsShortBase64Secret() {
        JwtService service = new JwtService(properties("base64:c2hvcnQ="));

        assertThrows(IllegalStateException.class, service::validateSigningKey);
    }

    @Test
    void generatesAndParsesTokenWithStrongRawSecret() {
        JwtService service = new JwtService(properties("not-a-base64-secret-with-32-bytes!!"));
        service.validateSigningKey();

        String token = service.generateToken("user-1", "player@example.com", "admin");
        Claims claims = service.parse(token);

        assertEquals("user-1", claims.getSubject());
        assertEquals("player@example.com", claims.get("email"));
        assertEquals("admin", claims.get("role"));
    }

    private AppProperties properties(String secret) {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret(secret);
        properties.getJwt().setExpirationMs(60_000);
        return properties;
    }
}
