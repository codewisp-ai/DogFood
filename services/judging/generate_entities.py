import os

base_dir = "/Users/yash/Desktop/dogfood/services/judging/src/main/java/com/dogfood/judging/entity"

entities = {
    "Rubric.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "rubrics")
public class Rubric {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}
""",
    "Criterion.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "criteria")
public class Criterion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "rubric_id")
    private UUID rubricId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;
    
    @Column(name = "max_score")
    private Integer maxScore;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
}
""",
    "JudgeAssignment.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "judge_assignments")
public class JudgeAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    @Column(name = "batch_id")
    private UUID batchId;
    
    @Column(name = "track_id")
    private UUID trackId;
    
    private String status; // PENDING, IN_PROGRESS, COMPLETED, RECUSED
    
    @Column(name = "assigned_at", insertable = false, updatable = false)
    private ZonedDateTime assignedAt;
}
""",
    "Score.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "scores")
public class Score {
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
    
    @Column(name = "raw_score", nullable = false)
    private Integer rawScore;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
""",
    "NormalizedScore.java": """package com.dogfood.judging.entity;

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
""",
    "FinalScore.java": """package com.dogfood.judging.entity;

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
""",
    "CalibrationSubmission.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "calibration_submissions")
public class CalibrationSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
}
""",
    "CalibrationReferenceScore.java": """package com.dogfood.judging.entity;

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
""",
    "CalibrationResult.java": """package com.dogfood.judging.entity;

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
""",
    "ConflictOfInterest.java": """package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "conflict_of_interest")
public class ConflictOfInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    
    @Column(name = "judge_id", nullable = false)
    private UUID judgeId;
    
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    
    private String reason;
    
    @Column(name = "declared_at", insertable = false, updatable = false)
    private ZonedDateTime declaredAt;
}
""",
    "SubmissionFlag.java": """package com.dogfood.judging.entity;

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
"""
}

for path, content in entities.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w") as f:
        f.write(content)
