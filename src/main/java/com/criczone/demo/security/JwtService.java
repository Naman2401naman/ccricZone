package com.criczone.demo.security;

import com.criczone.demo.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateToken(String userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(new Date(now))
            .expiration(new Date(now + appProperties.getJwt().getExpirationMs()))
            .signWith((SecretKey) getSigningKey())
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Key getSigningKey() {
        String secret = appProperties.getJwt().getSecret();
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException | DecodingException ignored) {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                byte[] padded = new byte[32];
                System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, padded.length));
                for (int i = bytes.length; i < padded.length; i++) {
                    padded[i] = 'x';
                }
                bytes = padded;
            }
            return Keys.hmacShaKeyFor(bytes);
        }
    }
}
