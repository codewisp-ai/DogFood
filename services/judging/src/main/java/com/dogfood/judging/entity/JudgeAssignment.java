package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "judge_assignments")
public class JudgeAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    @Column(name = "batch_id")
    private UUID batchId;
    
    @Column(name = "track_id")
    private UUID trackId;
    
    private String status; // PENDING, IN_PROGRESS, COMPLETED, RECUSED
    
    @Column(name = "assigned_at", insertable = false, updatable = false)
    private ZonedDateTime assignedAt;
}
