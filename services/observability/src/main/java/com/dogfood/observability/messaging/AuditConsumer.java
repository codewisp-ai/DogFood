package com.dogfood.observability.messaging;
import com.dogfood.observability.entity.AuditRecord;
import com.dogfood.observability.repository.AuditRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditConsumer {
    private final AuditRecordRepository repository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "audit.queue")
    public void consumeAudit(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            AuditRecord record = AuditRecord.builder()
                .eventType((String) payload.getOrDefault("eventType", "unknown"))
                .entityId(payload.containsKey("entityId") && payload.get("entityId") != null ? UUID.fromString((String) payload.get("entityId")) : UUID.randomUUID())
                .payload(payload)
                .createdAt(OffsetDateTime.now())
                .build();
            repository.save(record);
            log.info("Saved audit record: {}", record.getEventType());
        } catch (Exception e) {
            log.error("Failed to process audit message: {}", message, e);
        }
    }
}
