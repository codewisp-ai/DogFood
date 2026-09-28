package com.dogfood.judging.repository;

import com.dogfood.judging.entity.CalibrationSubmission;
import com.dogfood.judging.entity.CalibrationReferenceScore;
import com.dogfood.judging.entity.CalibrationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalibrationSubmissionRepository extends JpaRepository<CalibrationSubmission, UUID> {
    List<CalibrationSubmission> findByEventId(UUID eventId);
}

