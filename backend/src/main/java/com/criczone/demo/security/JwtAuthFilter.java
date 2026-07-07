package com.criczone.demo.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.UserRepository;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final LoadingCache<String, Optional<UserDocument>> userCache;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(10_000)
            .build(userRepository::findById);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parse(authHeader.substring(7));
            String userId = claims.getSubject();
            Optional<UserDocument> userOptional = userCache.get(userId);
            if (userOptional.isPresent()) {
                UserDocument user = userOptional.get();
                AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getRole());
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute("currentUser", user);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
