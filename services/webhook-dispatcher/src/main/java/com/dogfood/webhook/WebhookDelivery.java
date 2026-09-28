package com.dogfood.webhook;
import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhook_deliveries", schema = "webhooks")
@Data
public class WebhookDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID endpointId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;
    
    private String status = "PENDING";
    private int attempts = 0;
    private ZonedDateTime lastAttemptAt;
    private Integer responseCode;
    private String errorMessage;
    @Column(updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
