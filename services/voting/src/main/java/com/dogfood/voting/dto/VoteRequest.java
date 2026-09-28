package com.dogfood.voting.dto;

import java.util.UUID;

public record VoteRequest(
    UUID submissionId,
    Integer votesToCast,
    String deviceFingerprint
) {}
