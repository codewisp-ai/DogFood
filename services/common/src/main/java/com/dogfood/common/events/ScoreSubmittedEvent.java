package com.dogfood.common.events;

import java.time.Instant;
import java.util.UUID;

/** Published when a score is submitted, triggers async normalization recompute. */
public record ScoreSubmittedEvent(
        UUID eventId,
        UUID judgeId,
        UUID submissionId,
        UUID criterionId,
        int rawScore,
        Instant timestamp
) {
    public ScoreSubmittedEvent {
        if (timestamp == null) timestamp = Instant.now();
    }
}
