package com.dogfood.submission.dto;

import com.dogfood.submission.entity.SubmissionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SubmissionResponse(
    UUID id,
    UUID eventId,
    UUID teamId,
    UUID trackId,
    String name,
    String tagline,
    String description,
    String thumbnailUrl,
    List<String> imageGallery,
    String demoVideoUrl,
    String repositoryUrl,
    String liveLink,
    List<String> techTags,
    Map<String, Object> customAnswers,
    SubmissionStatus status,
    String forensicStatus,
    Double riskScore,
    List<String> riskFlags,
    Instant submittedAt,
    Instant createdAt,
    Instant updatedAt
) {}
