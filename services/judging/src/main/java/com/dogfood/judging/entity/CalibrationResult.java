package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "calibration_results")
public class CalibrationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "calibration_submission_id")
    private UUID calibrationSubmissionId;
    
    @Column(name = "criterion_id")
    private UUID criterionId;
    
    @Column(name = "judge_score", nullable = false)
    private Integer judgeScore;
    
    @Column(name = "reference_score", nullable = false)
    private Integer referenceScore;
    
    @Column(nullable = false)
    private Integer deviation;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}
