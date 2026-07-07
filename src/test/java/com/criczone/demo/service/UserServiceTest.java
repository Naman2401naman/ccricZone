package com.criczone.demo.service;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.UserRepository;
import com.criczone.demo.security.JwtService;
import com.criczone.demo.support.ApiException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void registerRejectsDuplicateEmailBeforeSaving() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        UserService userService = new UserService(userRepository, passwordEncoder, jwtService);

        when(userRepository.findByEmail("player@example.com")).thenReturn(Optional.of(new UserDocument()));

        ApiException error = assertThrows(ApiException.class, () -> userService.register(Map.of(
            "name", "Player",
            "email", "player@example.com",
            "phone", "9876543210",
            "password", "secret")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("User already exists", error.getMessage());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void forgotAndResetPasswordUpdatesStoredPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        UserService userService = new UserService(userRepository, passwordEncoder, jwtService);
        UserDocument user = new UserDocument();
        user.setId("user-1");
        user.setEmail("player@example.com");

        when(userRepository.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("hashed-password");

        Map<String, Object> forgotResponse = userService.forgotPassword(Map.of("email", "player@example.com"));
        String token = String.valueOf(forgotResponse.get("resetToken"));
        userService.resetPassword(token, Map.of("password", "new-password", "confirmPassword", "new-password"));

        assertTrue(token.matches("(?i)^[a-f0-9]{64}$"));
        assertEquals("hashed-password", user.getPassword());
        verify(userRepository).save(user);
    }
}
