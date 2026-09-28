package com.dogfood.submission.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateSubmissionRequest(
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
    Map<String, Object> customAnswers
) {}
