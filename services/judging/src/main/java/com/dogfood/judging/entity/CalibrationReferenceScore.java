package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "calibration_reference_scores")
public class CalibrationReferenceScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "calibration_submission_id")
    private UUID calibrationSubmissionId;
    
    @Column(name = "criterion_id")
    private UUID criterionId;
    
    @Column(name = "reference_score", nullable = false)
    private Integer referenceScore;
}
