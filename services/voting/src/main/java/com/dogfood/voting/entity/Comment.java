package com.dogfood.voting.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "comments", schema = "voting")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID eventId;
    private UUID submissionId;
    private UUID userId;
    
    @Column(length = 2000)
    private String content;
    
    @Column(insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
