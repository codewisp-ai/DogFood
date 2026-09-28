package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "final_scores")
public class FinalScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "submission_id", nullable = false, unique = true)
    private UUID submissionId;
    
    @Column(name = "weighted_score", precision = 10, scale = 6)
    private BigDecimal weightedScore;
    
    @Column(name = "display_score", precision = 10, scale = 4)
    private BigDecimal displayScore;
    
    private Integer rank;
    
    @Column(name = "judge_count")
    private Integer judgeCount;
    
    @Column(name = "computed_at", insertable = false, updatable = false)
    private ZonedDateTime computedAt;
}
