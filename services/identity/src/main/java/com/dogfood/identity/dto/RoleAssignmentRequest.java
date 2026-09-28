package com.dogfood.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RoleAssignmentRequest(
    @NotNull UUID userId,
    @NotNull UUID eventId,
    @NotBlank String role,
    List<UUID> assignedTrackIds
) {}
