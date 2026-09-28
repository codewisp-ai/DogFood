package com.dogfood.judging.repository;

import com.dogfood.judging.entity.SubmissionFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubmissionFlagRepository extends JpaRepository<SubmissionFlag, UUID> {
    List<SubmissionFlag> findByEventId(UUID eventId);
    List<SubmissionFlag> findByEventIdAndStatus(UUID eventId, String status);
    boolean existsByJudgeIdAndSubmissionId(UUID judgeId, UUID submissionId);
}
