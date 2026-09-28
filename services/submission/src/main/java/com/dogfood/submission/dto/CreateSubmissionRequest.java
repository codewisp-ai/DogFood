package com.dogfood.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateSubmissionRequest(
    @NotNull UUID eventId,
    @NotNull UUID teamId,
    UUID trackId,
    @NotBlank String name,
    String tagline,
    String description,
    String thumbnailUrl,
    List<String> imageGallery,
    String demoVideoUrl,
    String repositoryUrl,
    String liveLink,
    List<String> techTags,
    Map<String, Object> customAnswers
) {}
