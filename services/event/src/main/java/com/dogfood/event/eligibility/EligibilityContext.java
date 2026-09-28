package com.dogfood.event.eligibility;

import java.util.UUID;

public record EligibilityContext(
    UUID teamId,
    UUID eventId,
    long teamSize,
    long existingSubmissionCount
) {}
