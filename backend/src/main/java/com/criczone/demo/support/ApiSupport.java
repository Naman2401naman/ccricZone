package com.criczone.demo.support;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.security.AuthUser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class ApiSupport {

    private ApiSupport() {
    }

    public static UserDocument requireUser(UserDocument user) {
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
        return user;
    }

    public static void requireRole(UserDocument user, String... roles) {
        requireUser(user);
        Set<String> allowed = Set.of(roles);
        if (!allowed.contains(String.valueOf(user.getRole()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied - allowed roles: " + String.join(", ", roles));
        }
    }

    public static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser)) {
            return null;
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authUser.getId();
    }

    public static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> updates) {
        Map<String, Object> result = new LinkedHashMap<>(base);
        updates.forEach((key, value) -> {
            if (value instanceof Map<?, ?> && result.get(key) instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedCurrent = (Map<String, Object>) result.get(key);
                @SuppressWarnings("unchecked")
                Map<String, Object> typedUpdate = (Map<String, Object>) value;
                result.put(key, deepMerge(typedCurrent, typedUpdate));
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    public static String invoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static Instant parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }

    public static int parseTimeToMinutes(String value) {
        String normalized = Optional.ofNullable(value).orElse("").trim().toUpperCase().replace(" ", "");
        if (normalized.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid time format");
        }
        boolean am = normalized.endsWith("AM");
        boolean pm = normalized.endsWith("PM");
        String timePart = am || pm ? normalized.substring(0, normalized.length() - 2) : normalized;
        String[] parts = timePart.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        if (am && hour == 12) hour = 0;
        if (pm && hour < 12) hour += 12;
        return hour * 60 + minute;
    }

    public static double safeDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }

    public static int safeInt(Object value) {
        return (int) Math.round(safeDouble(value));
    }

    public static String trim(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static String ballsToOvers(int balls) {
        int whole = Math.max(0, balls) / 6;
        int rem = Math.max(0, balls) % 6;
        return whole + "." + rem;
    }

    public static int oversToBalls(String overs) {
        if (overs == null || overs.isBlank()) return 0;
        String[] parts = overs.split("\\.");
        int whole = Integer.parseInt(parts[0]);
        int rem = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return whole * 6 + rem;
    }

    public static Map<String, Object> playerPublic(UserDocument user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("_id", user.getId());
        payload.put("name", user.getName());
        payload.put("email", user.getEmail());
        payload.put("phone", user.getPhone());
        payload.put("role", user.getRole());
        payload.put("profile", user.getProfile());
        payload.put("stats", user.getStats());
        payload.put("formatStats", user.getFormatStats());
        payload.put("rankings", user.getRankings());
        payload.put("media", user.getMedia());
        payload.put("social", user.getSocial());
        payload.put("teams", user.getTeams());
        payload.put("tournaments", user.getTournaments());
        payload.put("matchHistory", user.getMatchHistory());
        payload.put("notifications", user.getNotifications());
        payload.put("createdAt", user.getCreatedAt());
        payload.put("updatedAt", user.getUpdatedAt());
        payload.put("followerCount", ((List<?>) user.getSocial().getOrDefault("followers", List.of())).size());
        payload.put("followingCount", ((List<?>) user.getSocial().getOrDefault("following", List.of())).size());
        return payload;
    }

    public static List<Map<String, Object>> normalizeMembers(List<Map<String, Object>> rawMembers) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rawMembers == null) {
            return result;
        }
        for (Map<String, Object> member : rawMembers) {
            if (member == null) continue;
            String name = trim(member.get("name"));
            String email = trim(member.get("email")).toLowerCase();
            Object rawId = member.get("userId") != null ? member.get("userId") : member.get("playerId");
            String playerId = trim(rawId);
            if (name.isBlank() && email.isBlank() && playerId.isBlank()) continue;
            Map<String, Object> normalized = new HashMap<>();
            normalized.put("name", name.isBlank() ? (email.isBlank() ? "Player" : email.split("@")[0]) : name);
            normalized.put("email", email.isBlank() ? null : email);
            normalized.put("userId", playerId.isBlank() ? null : playerId);
            result.add(normalized);
        }
        return result;
    }

    public static Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }

    public static boolean sameId(Object left, Object right) {
        return Objects.equals(trim(left), trim(right));
    }
}
