package com.dogfood.voting.repository;

import com.dogfood.voting.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteResultRepository extends JpaRepository<VoteResult, UUID> {
    List<VoteResult> findByEventId(UUID eventId);
    Optional<VoteResult> findByEventIdAndSubmissionId(UUID eventId, UUID submissionId);
}
