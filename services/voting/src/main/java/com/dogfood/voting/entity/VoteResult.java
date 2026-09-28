package com.dogfood.voting.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "vote_results", schema = "voting")
public class VoteResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID eventId;
    private UUID submissionId;
    
    private Integer totalVotes = 0;
    private BigDecimal totalInfluence = BigDecimal.ZERO;
    
    @Column(name = "rank")
    private Integer rank;
    
    @Column(insertable = false, updatable = false)
    private OffsetDateTime computedAt;
}
