package com.dogfood.event.bulk;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class BulkController {

    private final ExportJobRepository exportJobRepository;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/{eventId}/bulk-export")
    public ResponseEntity<ExportJob> requestBulkExport(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
            
        if (roles == null || !roles.contains("ORGANIZER")) {
            return ResponseEntity.status(401).build();
        }

        UUID requesterId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();

        ExportJob job = ExportJob.builder()
                .eventId(eventId)
                .requesterId(requesterId)
                .status("PENDING")
                .build();
        exportJobRepository.save(job);

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend("dogfood.events", "event.export.requested", 
                Map.of("jobId", job.getId(), "eventId", eventId, "requesterId", requesterId));

        return ResponseEntity.accepted().body(job);
    }
    
    @GetMapping("/{eventId}/bulk-export")
    public ResponseEntity<List<ExportJob>> getBulkExports(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (roles == null || !roles.contains("ORGANIZER")) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(exportJobRepository.findByEventIdOrderByCreatedAtDesc(eventId));
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<Map<String, String>> requestBulkImport(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
            
        if (roles == null || (!roles.contains("ORGANIZER") && !roles.contains("ADMIN"))) {
            return ResponseEntity.status(401).build();
        }

        String s3Url = payload.get("s3Url");
        if (s3Url == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "s3Url is required"));
        }

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend("dogfood.events", "event.import.requested", 
                Map.of("s3Url", s3Url));

        return ResponseEntity.accepted().body(Map.of(
            "status", "ACCEPTED", 
            "message", "Bulk import job queued asynchronously via RabbitMQ."
        ));
    }
}
