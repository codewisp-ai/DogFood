package com.dogfood.judging.repository;

import com.dogfood.judging.entity.CalibrationReferenceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalibrationReferenceScoreRepository extends JpaRepository<CalibrationReferenceScore, UUID> {
    List<CalibrationReferenceScore> findByCalibrationSubmissionId(UUID calibrationSubmissionId);
}
