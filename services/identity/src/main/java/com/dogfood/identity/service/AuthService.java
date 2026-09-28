package com.dogfood.identity.service;

import com.dogfood.identity.dto.*;
import com.dogfood.identity.entity.RefreshToken;
import com.dogfood.identity.entity.User;
import com.dogfood.identity.entity.UserEventRole;
import com.dogfood.identity.repository.RefreshTokenRepository;
import com.dogfood.identity.repository.UserEventRoleRepository;
import com.dogfood.identity.repository.UserRepository;
import com.dogfood.identity.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        if (request.role() != null && !request.role().isBlank()) {
            UserEventRole eventRole = UserEventRole.builder()
                    .user(user)
                    .eventId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .role(request.role().toUpperCase())
                    .build();
            userEventRoleRepository.save(eventRole);
        }

        publishAuditEvent("user.registered", Map.of("userId", user.getId(), "email", user.getEmail()));

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        publishAuditEvent("user.login", Map.of("userId", user.getId(), "email", user.getEmail()));

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = token.getUser();
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr == null) return;
        String tokenHash = hashToken(refreshTokenStr);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void assignRole(RoleAssignmentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<UserEventRole> existing = userEventRoleRepository.findByUserIdAndEventId(user.getId(), request.eventId());
        UserEventRole role = existing.orElseGet(() -> UserEventRole.builder()
                .user(user)
                .eventId(request.eventId())
                .build());

        role.setRole(request.role());
        role.setAssignedTrackIds(request.assignedTrackIds());
        userEventRoleRepository.save(role);

        publishAuditEvent("user.role.assigned", Map.of(
                "userId", user.getId(),
                "eventId", request.eventId(),
                "role", request.role()
        ));
    }

    private AuthResponse generateAuthResponse(User user) {
        List<UserEventRole> roles = userEventRoleRepository.findByUserId(user.getId());
        String accessToken = jwtService.createAccessToken(user, roles);
        String refreshTokenStr = jwtService.createRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshTokenStr))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.isEmailVerified()
        );

        return new AuthResponse(accessToken, refreshTokenStr, 900, userResponse);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void publishAuditEvent(String action, Map<String, Object> data) {
        try {
            com.dogfood.common.events.AuditEvent event = com.dogfood.common.events.AuditEvent.of(
                    action,
                    "user",
                    data.containsKey("userId") ? (java.util.UUID) data.get("userId") : null,
                    null,
                    "SYSTEM",
                    com.dogfood.common.security.RequestContext.getCorrelationId(),
                    data
            );
            // Publishing audit event to dogfood.audit exchange
            rabbitTemplate.convertAndSend("dogfood.audit", action, event);
        } catch (Exception e) {
            log.error("Failed to publish audit event", e);
        }
    }
}
