package com.dogfood.judging.repository;

import com.dogfood.judging.entity.FinalScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinalScoreRepository extends JpaRepository<FinalScore, UUID> {

    List<FinalScore> findByEventIdOrderByRankAsc(UUID eventId);

    Optional<FinalScore> findByEventIdAndSubmissionId(UUID eventId, UUID submissionId);
}
