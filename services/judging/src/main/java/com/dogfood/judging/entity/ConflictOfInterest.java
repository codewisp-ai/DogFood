package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "conflict_of_interest")
public class ConflictOfInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    private String reason;
    
    @Column(name = "declared_at", insertable = false, updatable = false)
    private ZonedDateTime declaredAt;
}
