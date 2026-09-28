package com.dogfood.notification;
import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "audit")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private boolean isRead = false;
    @Column(updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
