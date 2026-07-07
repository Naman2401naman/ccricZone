package com.criczone.demo.security;

import com.criczone.demo.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_SIGNING_KEY_BYTES = 32;
    private static final String PLACEHOLDER_SECRET = "change-me-change-me-change-me-change-me";
    private static final String BASE64_PREFIX = "base64:";

    private final AppProperties appProperties;
    private SecretKey signingKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void validateSigningKey() {
        this.signingKey = Keys.hmacShaKeyFor(resolveSigningKeyBytes());
    }

    public String generateToken(String userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(new Date(now))
            .expiration(new Date(now + appProperties.getJwt().getExpirationMs()))
            .signWith(getSigningKey())
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            validateSigningKey();
        }
        return signingKey;
    }

    private byte[] resolveSigningKeyBytes() {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.trim().isEmpty() || PLACEHOLDER_SECRET.equals(secret.trim())) {
            throw new IllegalStateException("JWT_SECRET must be set to a non-placeholder secret of at least 32 bytes");
        }

        String trimmed = secret.trim();
        byte[] bytes;
        if (trimmed.startsWith(BASE64_PREFIX)) {
            bytes = Decoders.BASE64.decode(trimmed.substring(BASE64_PREFIX.length()));
        } else {
            bytes = tryDecodeBase64(trimmed)
                .filter(decoded -> decoded.length >= MIN_SIGNING_KEY_BYTES)
                .orElseGet(() -> trimmed.getBytes(StandardCharsets.UTF_8));
        }

        if (bytes.length < MIN_SIGNING_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET must resolve to at least 32 bytes for HS256 signing");
        }
        return bytes;
    }

    private Optional<byte[]> tryDecodeBase64(String secret) {
        try {
            return Optional.of(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException | DecodingException ignored) {
            return Optional.empty();
        }
    }
}
