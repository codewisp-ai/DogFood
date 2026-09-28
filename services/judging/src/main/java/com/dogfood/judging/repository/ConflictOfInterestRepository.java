package com.dogfood.judging.repository;

import com.dogfood.judging.entity.ConflictOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConflictOfInterestRepository extends JpaRepository<ConflictOfInterest, UUID> {
    List<ConflictOfInterest> findByJudgeId(UUID judgeId);
    List<ConflictOfInterest> findByEventId(UUID eventId);
    Optional<ConflictOfInterest> findByJudgeIdAndSubmissionId(UUID judgeId, UUID submissionId);
    boolean existsByJudgeIdAndSubmissionId(UUID judgeId, UUID submissionId);
}
