package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "submission_flags")
public class SubmissionFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    @Column(nullable = false)
    private String reason;
    
    private String status; // PENDING, REVIEWED, DISMISSED
    
    @Column(name = "flagged_at", insertable = false, updatable = false)
    private ZonedDateTime flaggedAt;
}
