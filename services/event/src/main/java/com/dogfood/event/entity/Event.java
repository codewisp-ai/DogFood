package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String slug;
    private String description;
    
    @Column(name = "banner_url")
    private String bannerUrl;
    
    @Column(name = "organizer_id")
    private UUID organizerId;

    @Column(name = "registration_opens_at")
    private OffsetDateTime registrationOpensAt;

    @Column(name = "registration_closes_at")
    private OffsetDateTime registrationClosesAt;

    @Column(name = "submission_opens_at")
    private OffsetDateTime submissionOpensAt;

    @Column(name = "submission_deadline")
    private OffsetDateTime submissionDeadline;

    @Column(name = "judging_opens_at")
    private OffsetDateTime judgingOpensAt;

    @Column(name = "judging_closes_at")
    private OffsetDateTime judgingClosesAt;

    @Column(name = "voting_opens_at")
    private OffsetDateTime votingOpensAt;

    @Column(name = "voting_closes_at")
    private OffsetDateTime votingClosesAt;

    @Column(name = "results_published_at")
    private OffsetDateTime resultsPublishedAt;

    @Column(name = "judging_mode")
    private String judgingMode;

    @Column(name = "voting_mode")
    private String votingMode;

    @Column(name = "calibration_required")
    private Boolean calibrationRequired;

    @Column(name = "webhooks_enabled")
    private Boolean webhooksEnabled;

    @Column(name = "voting_access_mode")
    private String votingAccessMode;

    @Column(name = "quadratic_vote_budget")
    private Integer quadraticVoteBudget;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eligibility_rules", columnDefinition = "jsonb")
    private List<Map<String, Object>> eligibilityRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_questions", columnDefinition = "jsonb")
    private List<Map<String, Object>> customQuestions;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
