package com.dogfood.webhook;
import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_endpoints", schema = "webhooks")
@Data
public class WebhookEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID eventId;
    private String url;
    private String secret;
    private boolean active = true;
    @Column(updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
