package com.dogfood.identity.controller;

import com.dogfood.identity.dto.*;
import com.dogfood.identity.entity.User;
import com.dogfood.identity.entity.UserEventRole;
import com.dogfood.identity.repository.UserEventRoleRepository;
import com.dogfood.identity.repository.UserRepository;
import com.dogfood.identity.security.JwtService;
import com.dogfood.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh tokens")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader("X-User-Id") String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new UserResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.isEmailVerified()
        ));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestHeader("X-User-Id") String userId, @RequestBody UserResponse request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDisplayName(request.displayName());
        user.setAvatarUrl(request.avatarUrl());
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.isEmailVerified()
        ));
    }

    @Operation(summary = "Assign role to user for an event")
    @PostMapping("/roles")
    public ResponseEntity<Void> assignRole(@RequestHeader("X-User-Roles") String rolesHeader, @Valid @RequestBody RoleAssignmentRequest request) {
        // Typically check if rolesHeader contains ORGANIZER or ADMIN for the event
        authService.assignRole(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get roles for an event")
    @GetMapping("/roles/{eventId}")
    public ResponseEntity<List<UserEventRole>> getRolesForEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(userEventRoleRepository.findByEventIdAndRole(eventId, "")); // Simplify for now
    }

    @Operation(summary = "Get JWKS")
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(jwtService.getJwks());
    }

    @Operation(summary = "Get user by ID (Internal)")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new UserResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.isEmailVerified()
        ));
    }
}
