package com.dogfood.common.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base audit event published to the dogfood.audit RabbitMQ exchange.
 * Every service publishes these for every mutation, enabling a unified audit trail.
 */
public record AuditEvent(
        UUID eventId,
        String correlationId,
        UUID actorId,
        String actorRole,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> details,
        String ipAddress,
        Instant timestamp
) implements Serializable {
    public AuditEvent {
        if (timestamp == null) timestamp = Instant.now();
        if (eventId == null) eventId = UUID.randomUUID();
    }

    public static AuditEvent of(String action, String entityType, UUID entityId,
                                 UUID actorId, String actorRole, String correlationId) {
        return new AuditEvent(UUID.randomUUID(), correlationId, actorId, actorRole,
                action, entityType, entityId, Map.of(), null, Instant.now());
    }

    public static AuditEvent of(String action, String entityType, UUID entityId,
                                 UUID actorId, String actorRole, String correlationId,
                                 Map<String, Object> details) {
        return new AuditEvent(UUID.randomUUID(), correlationId, actorId, actorRole,
                action, entityType, entityId, details, null, Instant.now());
    }
}
