package com.dogfood.judging.repository;

import com.dogfood.judging.entity.NormalizedScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NormalizedScoreRepository extends JpaRepository<NormalizedScore, UUID> {

    Optional<NormalizedScore> findByJudgeIdAndSubmissionIdAndCriterionId(
            UUID judgeId, UUID submissionId, UUID criterionId);

    List<NormalizedScore> findByEventId(UUID eventId);

    List<NormalizedScore> findByEventIdAndSubmissionId(UUID eventId, UUID submissionId);

    @Query("SELECT ns FROM NormalizedScore ns WHERE ns.eventId = :eventId AND ns.judgeId = :judgeId")
    List<NormalizedScore> findByEventIdAndJudgeId(UUID eventId, UUID judgeId);
}
