package com.dogfood.voting.abuse;

import com.dogfood.voting.repository.VoteRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class DuplicateDetector {
    private final VoteRepository voteRepository;

    public DuplicateDetector(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public boolean isDuplicate(UUID eventId, UUID submissionId, UUID voterId) {
        return voteRepository.existsByEventIdAndSubmissionIdAndVoterId(eventId, submissionId, voterId);
    }
}
