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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            // Email already in use → 409 Conflict
            return ResponseEntity.status(409)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            // Invalid credentials → 401 Unauthorized
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));
        }
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
    public ResponseEntity<List<UserEventRole>> getRolesForEvent(
            @PathVariable UUID eventId,
            @RequestParam(required = false) String role) {
        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(userEventRoleRepository.findByEventIdAndRole(eventId, role.toUpperCase()));
        }
        return ResponseEntity.ok(userEventRoleRepository.findByEventId(eventId));
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
    @Operation(summary = "Export users for an event as CSV")
    @GetMapping(value = "/events/{eventId}/users/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportEventUsers(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        if (rolesHeader == null || !rolesHeader.contains("ORGANIZER")) {
            return ResponseEntity.status(403).build();
        }

        List<UserEventRole> roles = userEventRoleRepository.findByEventId(eventId);
        StringBuilder csv = new StringBuilder();
        csv.append("UserId,Email,DisplayName,Role,AssignedTrackIds\n");

        for (UserEventRole r : roles) {
            User u = r.getUser();
            csv.append(String.format("%s,%s,%s,%s,%s\n",
                    u.getId(),
                    escapeCsv(u.getEmail()),
                    escapeCsv(u.getDisplayName()),
                    r.getRole(),
                    r.getAssignedTrackIds() != null ? r.getAssignedTrackIds().toString().replace(",", ";") : ""
            ));
        }

        return ResponseEntity.ok(csv.toString());
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escaped = data.replaceAll("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
