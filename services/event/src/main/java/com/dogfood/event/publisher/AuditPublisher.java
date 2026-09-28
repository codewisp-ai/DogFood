package com.dogfood.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditPublisher {
    private final RabbitTemplate rabbitTemplate;
    // Assuming dogfood.audit exchange is defined in common config or manually
    private static final String EXCHANGE = "dogfood.audit";

    public void publishEvent(String eventType, UUID entityId, Map<String, Object> details) {
        try {
            com.dogfood.common.events.AuditEvent event = com.dogfood.common.events.AuditEvent.of(
                    eventType,
                    "event",
                    entityId,
                    null, // actorId
                    "SYSTEM", // actorRole
                    com.dogfood.common.security.RequestContext.getCorrelationId(),
                    details
            );
            rabbitTemplate.convertAndSend(EXCHANGE, eventType, event);
            log.info("Published audit event: {} for entity: {}", eventType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", eventType, e);
        }
    }
}
