package com.dogfood.submission.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "track_id")
    private UUID trackId;

    @Column(nullable = false)
    private String name;

    @Column(length = 300)
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    @Type(JsonType.class)
    @Column(name = "image_gallery", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> imageGallery = new ArrayList<>();

    @Column(name = "demo_video_url", length = 512)
    private String demoVideoUrl;

    @Column(name = "repository_url", length = 512)
    private String repositoryUrl;

    @Column(name = "live_link", length = 512)
    private String liveLink;

    @Type(ListArrayType.class)
    @Column(name = "tech_tags", columnDefinition = "text[]")
    @Builder.Default
    private List<String> techTags = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "custom_answers", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> customAnswers = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Integer version;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
