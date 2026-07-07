package com.criczone.demo.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_LIMIT_PER_MINUTE = 10;
    private static final int REGISTER_LIMIT_PER_MINUTE = 5;

    private final Cache<String, AtomicInteger> attempts = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .maximumSize(25_000)
        .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!isLimitedAuthPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        int limit = request.getRequestURI().toLowerCase(Locale.ROOT).contains("/register")
            || request.getRequestURI().toLowerCase(Locale.ROOT).contains("/signup")
            ? REGISTER_LIMIT_PER_MINUTE
            : LOGIN_LIMIT_PER_MINUTE;
        int count = attempts.asMap().computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        if (count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"success\":false,\"message\":\"Too many authentication attempts. Please try again in a minute.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimitedAuthPath(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI().toLowerCase(Locale.ROOT);
        return uri.equals("/api/users/login")
            || uri.equals("/api/users/register")
            || uri.equals("/api/users/signup");
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String clientIp = forwardedFor == null || forwardedFor.isBlank()
            ? request.getRemoteAddr()
            : forwardedFor.split(",")[0].trim();
        return clientIp + ":" + request.getRequestURI().toLowerCase(Locale.ROOT);
    }
}
