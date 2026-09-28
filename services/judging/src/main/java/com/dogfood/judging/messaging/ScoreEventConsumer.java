package com.dogfood.judging.messaging;

import com.dogfood.common.events.ScoreSubmittedEvent;
import com.dogfood.common.events.RabbitConstants;
import com.dogfood.judging.normalization.NormalizationEngine;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes score.submitted events and triggers async normalization recompute.
 * This decouples the score submission write path from the heavy normalization computation,
 * ensuring scoring stays fast even under concurrent load.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreEventConsumer {
    
    private final NormalizationEngine normalizationEngine;
    
    /**
     * Triggered when a judge submits a score.
     * Recomputes normalized and final scores for the entire event.
     * 
     * The recomputation is idempotent, so duplicate events are safe.
     * Failures are logged but don't interrupt the scoring flow.
     */
    @RabbitListener(queues = RabbitConstants.SCORE_NORMALIZATION_QUEUE)
    public void handleScoreSubmitted(ScoreSubmittedEvent event) {
        log.info("Received ScoreSubmittedEvent: eventId={}, judgeId={}, submissionId={}", 
                event.eventId(), event.judgeId(), event.submissionId());
        
        try {
            normalizationEngine.recompute(event.eventId());
            log.info("Successfully recomputed normalization for eventId={}", event.eventId());
        } catch (Exception e) {
            log.error("Failed to recompute normalization for eventId={}: {}", 
                    event.eventId(), e.getMessage(), e);
            // Don't rethrow - let RabbitMQ handle retry/DLQ based on configuration
        }
    }
}
