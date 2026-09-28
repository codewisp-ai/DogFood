package com.dogfood.event.dto;

import com.dogfood.event.entity.TeamRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public class TeamDtos {
    public record CreateTeamRequest(@NotBlank String name) {}
    public record TeamResponse(UUID id, UUID eventId, String name, UUID createdBy, OffsetDateTime createdAt) {}
    public record TeamMemberResponse(UUID id, UUID teamId, UUID userId, TeamRole role, OffsetDateTime joinedAt) {}
    
    public record CreateInviteRequest(String email) {}
    public record InviteResponse(UUID id, UUID teamId, String token, String email, OffsetDateTime expiresAt) {}
}
