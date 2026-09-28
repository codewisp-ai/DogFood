package com.dogfood.voting.dto;

import java.util.UUID;

public record CommentRequest(
    UUID eventId,
    String content
) {}
