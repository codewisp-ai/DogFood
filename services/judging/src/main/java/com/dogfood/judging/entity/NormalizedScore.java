package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "normalized_scores")
public class NormalizedScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    @Column(name = "criterion_id")
    private UUID criterionId;
    
    @Column(name = "z_score", precision = 10, scale = 6)
    private BigDecimal zScore;
    
    @Column(name = "shrinkage_adjusted_z", precision = 10, scale = 6)
    private BigDecimal shrinkageAdjustedZ;
    
    @Column(name = "judge_review_count")
    private Integer judgeReviewCount;
    
    @Column(name = "computed_at", insertable = false, updatable = false)
    private ZonedDateTime computedAt;
}
