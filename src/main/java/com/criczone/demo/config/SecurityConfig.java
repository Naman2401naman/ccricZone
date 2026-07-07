package com.criczone.demo.config;

import com.criczone.demo.security.AuthRateLimitFilter;
import com.criczone.demo.security.JwtAuthFilter;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, AuthRateLimitFilter authRateLimitFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .antMatchers(
                    "/",
                    "/index.html",
                    "/favicon.ico",
                    "/favicon.ico.png",
                    "/manifest.webmanifest",
                    "/sw.js",
                    "/runtime-config.js",
                    "/styles.css",
                    "/script.js",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**")
                .permitAll()
                .antMatchers("/icons/**", "/assets/**", "/css/**", "/js/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/health", "/api/version", "/api/matches/**", "/api/tournaments/**", "/api/turfs/**", "/api/leaderboard/**", "/api/posts/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/search-players", "/api/users/nearby-players", "/api/users/player/**", "/api/users/leaderboard/**").permitAll()
                .antMatchers(HttpMethod.POST,
                    "/api/users/login",
                    "/api/users/register",
                    "/api/users/signup",
                    "/api/users/forgot-password",
                    "/api/users/reset-password/**")
                .permitAll()
                .antMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AppProperties appProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        if (appProperties.isAllowAllOrigins()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(appProperties.getClientUrl());
            configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "https://*.onrender.com",
                "capacitor://localhost",
                "ionic://localhost"));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
