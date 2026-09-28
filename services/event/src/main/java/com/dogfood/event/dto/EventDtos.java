package com.dogfood.event.dto;

import com.dogfood.event.entity.EventStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventDtos {
    public record CreateEventRequest(
        @NotBlank String name,
        String description,
        String bannerUrl,
        @NotNull OffsetDateTime submissionDeadline,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions
    ) {}

    public record UpdateEventRequest(
        String name,
        String description,
        OffsetDateTime submissionDeadline,
        EventStatus status,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions
    ) {}

    public record EventResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String bannerUrl,
        UUID organizerId,
        OffsetDateTime submissionDeadline,
        EventStatus status,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {}

    public record CreateTrackRequest(@NotBlank String name, String description, Integer sortOrder) {}
    public record TrackResponse(UUID id, UUID eventId, String name, String description, Integer sortOrder) {}
}
