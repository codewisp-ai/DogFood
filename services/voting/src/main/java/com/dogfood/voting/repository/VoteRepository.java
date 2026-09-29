package com.dogfood.voting.repository;

import com.dogfood.voting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findByEventId(UUID eventId);
    List<Vote> findByEventIdAndVoterId(UUID eventId, UUID voterId);
    boolean existsByEventIdAndSubmissionIdAndVoterId(UUID eventId, UUID submissionId, UUID voterId);
}
