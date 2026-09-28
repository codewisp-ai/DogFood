package com.dogfood.voting.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "votes", schema = "voting")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID eventId;
    private UUID submissionId;
    private UUID voterId;
    private String voterEmail;
    
    @Column(name = "voter_ip")
    private String voterIp;
    
    private String deviceFingerprint;
    
    private Integer creditsSpent = 1;
    private BigDecimal voteInfluence = BigDecimal.ONE;
    
    @Column(insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
