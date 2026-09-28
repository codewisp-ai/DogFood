package com.dogfood.judging.repository;

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
    List<Score> findByJudgeId(UUID judgeId);
    List<Score> findByJudgeIdAndSubmissionId(UUID judgeId, UUID submissionId);
    Optional<Score> findByJudgeIdAndSubmissionIdAndCriterionId(UUID judgeId, UUID submissionId, UUID criterionId);
    List<Score> findByEventId(UUID eventId);
}
