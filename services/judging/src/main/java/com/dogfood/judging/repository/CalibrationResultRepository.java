package com.dogfood.judging.repository;

import com.dogfood.judging.entity.CalibrationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalibrationResultRepository extends JpaRepository<CalibrationResult, UUID> {
    List<CalibrationResult> findByEventIdAndJudgeId(UUID eventId, UUID judgeId);
    List<CalibrationResult> findByEventId(UUID eventId);
}
