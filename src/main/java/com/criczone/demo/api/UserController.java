package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.ForgotPasswordRequest;
import com.criczone.demo.dto.ApiRequests.LoginRequest;
import com.criczone.demo.dto.ApiRequests.RegisterRequest;
import com.criczone.demo.dto.ApiRequests.ResetPasswordRequest;
import com.criczone.demo.dto.ApiRequests.UpdateProfileRequest;
import com.criczone.demo.dto.ApiRequests.UpdateRoleRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
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
@Tag(name = "Users", description = "Registration, login, profiles, player discovery, and social actions")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"/register", "/signup"})
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(RequestMaps.toMap(request));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(RequestMaps.toMap(request));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return userService.logout(currentUser);
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(RequestMaps.toMap(request));
    }

    @PostMapping("/reset-password/{token}")
    public Map<String, Object> resetPassword(@PathVariable String token,
                                             @Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(token, RequestMaps.toMap(request));
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return userService.profile(currentUser);
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                             @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUser, RequestMaps.toMap(request));
    }

    @GetMapping("/player/{id}")
    public Map<String, Object> getPlayer(@PathVariable String id) {
        return userService.getPlayer(id);
    }

    @GetMapping("/search-players")
    public Map<String, Object> searchPlayers(@RequestParam(required = false) String playerType,
                                             @RequestParam(required = false) String location,
                                             @RequestParam(required = false) String bowlingStyle,
                                             @RequestParam(required = false) String battingStyle,
                                             @RequestParam(required = false) String availability,
                                             @RequestParam(required = false) String experienceLevel,
                                             @RequestParam(required = false) String search) {
        return userService.searchPlayers(playerType, location, bowlingStyle, battingStyle, availability, experienceLevel, search);
    }

    @GetMapping("/nearby-players")
    public Map<String, Object> nearbyPlayers(@RequestParam String city) {
        return userService.nearbyPlayers(city);
    }

    @GetMapping("/leaderboard/batsmen")
    public Map<String, Object> topBatsmen(@RequestParam(defaultValue = "10") int limit) {
        return userService.topBatsmen(limit);
    }

    @GetMapping("/leaderboard/bowlers")
    public Map<String, Object> topBowlers(@RequestParam(defaultValue = "10") int limit) {
        return userService.topBowlers(limit);
    }

    @GetMapping("/leaderboard/all-rounders")
    public Map<String, Object> topAllRounders(@RequestParam(defaultValue = "10") int limit) {
        return userService.topAllRounders(limit);
    }

    @PutMapping("/role")
    public Map<String, Object> updateRole(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @Valid @RequestBody UpdateRoleRequest request) {
        return userService.updateRole(currentUser, RequestMaps.toMap(request));
    }

    @PostMapping("/follow/{userId}")
    public Map<String, Object> follow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String userId) {
        return userService.follow(currentUser, userId);
    }

    @PostMapping("/unfollow/{userId}")
    public Map<String, Object> unfollow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String userId) {
        return userService.unfollow(currentUser, userId);
    }
}
