package com.dogfood.judging.repository;

import com.dogfood.judging.entity.JudgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, UUID> {
    List<JudgeAssignment> findByEventId(UUID eventId);
    List<JudgeAssignment> findByJudgeId(UUID judgeId);
    Optional<JudgeAssignment> findByJudgeIdAndSubmissionId(UUID judgeId, UUID submissionId);
}
