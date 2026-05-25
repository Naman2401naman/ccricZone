package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.UserRepository;
import com.criczone.demo.security.JwtService;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping({"/register", "/signup"})
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PLAYER_PUBLIC, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_SEARCH, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.GLOBAL_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.SYSTEM_HEALTH, allEntries = true)
    })
    public Map<String, Object> register(@RequestBody Map<String, Object> request) {
        String name = ApiSupport.trim(request.get("name"));
        String email = ApiSupport.trim(request.get("email")).toLowerCase(Locale.ROOT);
        String phone = ApiSupport.trim(request.get("phone"));
        String password = ApiSupport.trim(request.get("password"));

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "All fields are required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User already exists");
        }
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Phone already exists");
        }

        UserDocument user = new UserDocument();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.getProfile().put("displayName", name);
        user.getProfile().put("availability", "Available");
        user.getProfile().put("playerType", "Not specified");
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("_id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        response.put("token", token);
        response.put("user", Map.of("_id", user.getId(), "name", user.getName(), "email", user.getEmail(), "phone", user.getPhone(), "role", user.getRole()));
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        String email = ApiSupport.trim(request.get("email")).toLowerCase(Locale.ROOT);
        String password = ApiSupport.trim(request.get("password"));
        UserDocument user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("_id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        response.put("token", token);
        response.put("user", Map.of("_id", user.getId(), "name", user.getName(), "email", user.getEmail(), "phone", user.getPhone(), "role", user.getRole()));
        return response;
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        return Map.of("success", true, "user", ApiSupport.playerPublic(user));
    }

    @PutMapping("/profile")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PLAYER_PUBLIC, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_SEARCH, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.GLOBAL_LEADERBOARD, allEntries = true)
    })
    public Map<String, Object> updateProfile(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                             @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        if (request.containsKey("name")) {
            user.setName(ApiSupport.trim(request.get("name")));
        }
        if (request.get("phone") != null) {
            user.setPhone(ApiSupport.trim(request.get("phone")));
        }
        if (request.get("profile") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) request.get("profile");
            user.setProfile(ApiSupport.deepMerge(user.getProfile(), typed));
        }
        if (request.get("media") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) request.get("media");
            user.setMedia(ApiSupport.deepMerge(user.getMedia(), typed));
        }
        if (request.get("notifications") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) request.get("notifications");
            user.setNotifications(ApiSupport.deepMerge(user.getNotifications(), typed));
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return Map.of("success", true, "message", "Profile updated successfully", "user", ApiSupport.playerPublic(user));
    }

    @GetMapping("/player/{id}")
    @Cacheable(cacheNames = CacheNames.PLAYER_PUBLIC, key = "#id")
    public Map<String, Object> getPlayer(@PathVariable String id) {
        UserDocument user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Player not found"));
        return Map.of("success", true, "player", ApiSupport.playerPublic(user));
    }

    @GetMapping("/search-players")
    @Cacheable(
        cacheNames = CacheNames.PLAYER_SEARCH,
        key = "T(java.lang.String).join('|', T(java.util.Arrays).asList(#playerType, #location, #bowlingStyle, #battingStyle, #availability, #experienceLevel, #search))")
    public Map<String, Object> searchPlayers(@RequestParam(required = false) String playerType,
                                             @RequestParam(required = false) String location,
                                             @RequestParam(required = false) String bowlingStyle,
                                             @RequestParam(required = false) String battingStyle,
                                             @RequestParam(required = false) String availability,
                                             @RequestParam(required = false) String experienceLevel,
                                             @RequestParam(required = false) String search) {
        List<Map<String, Object>> players = userRepository.findAll().stream()
            .filter(user -> matchesProfile(user, playerType, location, bowlingStyle, battingStyle, availability, experienceLevel, search))
            .sorted(Comparator.comparingDouble(this::rankingOverall).reversed())
            .limit(50)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", players.size(), "data", players);
    }

    @GetMapping("/nearby-players")
    @Cacheable(cacheNames = CacheNames.PLAYER_NEARBY, key = "#city")
    public Map<String, Object> nearbyPlayers(@RequestParam String city) {
        List<Map<String, Object>> players = userRepository.findAll().stream()
            .filter(user -> containsIgnoreCase(stringFromMap(user.getProfile(), "location", "city"), city))
            .filter(user -> {
                String availability = String.valueOf(user.getProfile().getOrDefault("availability", "Available"));
                return List.of("Available", "Looking for team").contains(availability);
            })
            .limit(30)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", players.size(), "data", players);
    }

    @GetMapping("/leaderboard/batsmen")
    @Cacheable(cacheNames = CacheNames.USER_LEADERBOARD, key = "'batsmen:' + #limit")
    public Map<String, Object> topBatsmen(@RequestParam(defaultValue = "10") int limit) {
        return Map.of("success", true, "count", limit, "leaderboard", sortUsersByMetric("batting", "runs", limit));
    }

    @GetMapping("/leaderboard/bowlers")
    @Cacheable(cacheNames = CacheNames.USER_LEADERBOARD, key = "'bowlers:' + #limit")
    public Map<String, Object> topBowlers(@RequestParam(defaultValue = "10") int limit) {
        return Map.of("success", true, "count", limit, "leaderboard", sortUsersByMetric("bowling", "wickets", limit));
    }

    @GetMapping("/leaderboard/all-rounders")
    @Cacheable(cacheNames = CacheNames.USER_LEADERBOARD, key = "'all-rounders:' + #limit")
    public Map<String, Object> topAllRounders(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = userRepository.findAll().stream()
            .filter(user -> "All-rounder".equalsIgnoreCase(String.valueOf(user.getProfile().getOrDefault("playerType", ""))))
            .sorted(Comparator.comparingDouble(this::rankingAllRounder).reversed())
            .limit(limit)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", data.size(), "leaderboard", data);
    }

    @PutMapping("/role")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PLAYER_PUBLIC, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_SEARCH, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.GLOBAL_LEADERBOARD, allEntries = true)
    })
    public Map<String, Object> updateRole(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @RequestBody Map<String, Object> request) {
        ApiSupport.requireRole(currentUser, "admin");
        String userId = ApiSupport.trim(request.get("userId"));
        String role = ApiSupport.trim(request.get("role"));
        UserDocument user = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return Map.of("success", true, "message", "User role updated successfully", "user", ApiSupport.playerPublic(user));
    }

    @PostMapping("/follow/{userId}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PLAYER_PUBLIC, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_SEARCH, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.GLOBAL_LEADERBOARD, allEntries = true)
    })
    public Map<String, Object> follow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String userId) {
        UserDocument me = ApiSupport.requireUser(currentUser);
        if (Objects.equals(me.getId(), userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }
        UserDocument other = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> following = stringList(me.getSocial().get("following"));
        if (!following.contains(userId)) {
            following.add(userId);
            me.getSocial().put("following", following);
        }
        List<String> followers = stringList(other.getSocial().get("followers"));
        if (!followers.contains(me.getId())) {
            followers.add(me.getId());
            other.getSocial().put("followers", followers);
        }
        userRepository.save(me);
        userRepository.save(other);
        return Map.of("success", true, "message", "User followed successfully");
    }

    @PostMapping("/unfollow/{userId}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PLAYER_PUBLIC, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_SEARCH, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PLAYER_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LEADERBOARD, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.GLOBAL_LEADERBOARD, allEntries = true)
    })
    public Map<String, Object> unfollow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String userId) {
        UserDocument me = ApiSupport.requireUser(currentUser);
        UserDocument other = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        me.getSocial().put("following", stringList(me.getSocial().get("following")).stream().filter(id -> !id.equals(userId)).collect(Collectors.toList()));
        other.getSocial().put("followers", stringList(other.getSocial().get("followers")).stream().filter(id -> !id.equals(me.getId())).collect(Collectors.toList()));
        userRepository.save(me);
        userRepository.save(other);
        return Map.of("success", true, "message", "User unfollowed successfully");
    }

    private List<Map<String, Object>> sortUsersByMetric(String section, String metric, int limit) {
        return userRepository.findAll().stream()
            .sorted(Comparator.comparingDouble((UserDocument user) -> metricValue(user, section, metric)).reversed())
            .limit(limit)
            .map(ApiSupport::playerPublic)
            .collect(Collectors.toList());
    }

    private double metricValue(UserDocument user, String section, String metric) {
        Object statsSection = user.getStats().get(section);
        if (statsSection instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) statsSection;
            return ApiSupport.safeDouble(map.get(metric));
        }
        return 0;
    }

    private double rankingOverall(UserDocument user) {
        return ApiSupport.safeDouble(user.getRankings().get("overall"));
    }

    private double rankingAllRounder(UserDocument user) {
        return ApiSupport.safeDouble(user.getRankings().get("allRounder"));
    }

    private boolean matchesProfile(UserDocument user, String playerType, String location, String bowlingStyle, String battingStyle,
                                   String availability, String experienceLevel, String search) {
        Map<String, Object> profile = user.getProfile();
        if (!playerTypeBlank(playerType) && !playerType.equalsIgnoreCase(String.valueOf(profile.get("playerType")))) return false;
        if (!playerTypeBlank(bowlingStyle) && !bowlingStyle.equalsIgnoreCase(String.valueOf(profile.get("bowlingStyle")))) return false;
        if (!playerTypeBlank(battingStyle) && !battingStyle.equalsIgnoreCase(String.valueOf(profile.get("battingStyle")))) return false;
        if (!playerTypeBlank(availability) && !availability.equalsIgnoreCase(String.valueOf(profile.get("availability")))) return false;
        if (!playerTypeBlank(experienceLevel) && !experienceLevel.equalsIgnoreCase(String.valueOf(profile.get("experienceLevel")))) return false;
        if (!playerTypeBlank(location) && !containsIgnoreCase(stringFromMap(profile, "location", "city"), location)) return false;
        if (!playerTypeBlank(search)) {
            String displayName = String.valueOf(profile.getOrDefault("displayName", ""));
            if (!containsIgnoreCase(user.getName(), search) && !containsIgnoreCase(displayName, search)) return false;
        }
        return true;
    }

    private boolean containsIgnoreCase(String actual, String probe) {
        return Optional.ofNullable(actual).orElse("").toLowerCase(Locale.ROOT).contains(Optional.ofNullable(probe).orElse("").toLowerCase(Locale.ROOT));
    }

    private String stringFromMap(Map<String, Object> source, String parent, String child) {
        Object obj = source.get(parent);
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            return String.valueOf(map.get(child) == null ? "" : map.get(child));
        }
        return "";
    }

    private boolean playerTypeBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?>)) return new ArrayList<>();
        List<?> list = (List<?>) value;
        return list.stream().map(String::valueOf).collect(Collectors.toCollection(ArrayList::new));
    }
}
