package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "rubrics")
public class Rubric {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;
    
    @Column(name = "normalization_enabled", nullable = false)
    private Boolean normalizationEnabled = true;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}
