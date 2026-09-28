import os

base_dir = "/Users/yash/Desktop/dogfood/services/judging/src/main/java/com/dogfood/judging"

files = {
    "repository/ScoreRepository.java": """package com.dogfood.judging.repository;

import com.dogfood.judging.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Optional<Score> findByIdempotencyKey(String idempotencyKey);
    List<Score> findByEventIdAndJudgeId(UUID eventId, UUID judgeId);
}
""",
    "repository/JudgeAssignmentRepository.java": """package com.dogfood.judging.repository;

import com.dogfood.judging.entity.JudgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, UUID> {
    List<JudgeAssignment> findByEventId(UUID eventId);
}
""",
    "normalization/NormalizationEngine.java": """package com.dogfood.judging.normalization;

import org.springframework.stereotype.Component;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NormalizationEngine {
    
    public void computeZScores(UUID eventId, UUID judgeId, UUID criterionId) {
        log.info("Computing Z-scores for event={}, judge={}, criterion={}", eventId, judgeId, criterionId);
    }
    
    public void applyShrinkage(double zScore, int judgeReviewCount) {
        // Implementation
    }
    
    public void computeFinalScores(UUID eventId) {
        log.info("Computing final scores for event={}", eventId);
    }
    
    public void recompute(UUID eventId) {
        log.info("Recomputing for event={}", eventId);
    }
}
""",
    "service/ScoringService.java": """package com.dogfood.judging.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {
    
    public void submitScore(UUID eventId, UUID judgeId, UUID submissionId, UUID criterionId, Integer rawScore, String idempotencyKey) {
        log.info("Submitting score for judge={}, submission={}, criterion={}", judgeId, submissionId, criterionId);
    }
}
""",
    "messaging/ScoreEventConsumer.java": """package com.dogfood.judging.messaging;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreEventConsumer {
    // Add RabbitListener when configured
}
"""
}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w") as f:
        f.write(content)
