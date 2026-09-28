package com.dogfood.voting.strategy;

import com.dogfood.voting.dto.VoteRequest;
import com.dogfood.voting.entity.Vote;
import com.dogfood.voting.repository.VoteRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component("simpleVotingStrategy")
public class SimpleVotingStrategy implements VotingStrategy {
    private final VoteRepository voteRepository;

    public SimpleVotingStrategy(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @Override
    public Vote castVote(UUID eventId, UUID voterId, String voterIp, VoteRequest request) {
        Vote vote = new Vote();
        vote.setEventId(eventId);
        vote.setSubmissionId(request.submissionId());
        vote.setVoterId(voterId);
        vote.setVoterIp(voterIp);
        vote.setDeviceFingerprint(request.deviceFingerprint());
        vote.setCreditsSpent(1);
        vote.setVoteInfluence(BigDecimal.ONE);
        return voteRepository.save(vote);
    }

    @Override
    public void tallyVotes(UUID eventId) {
        // Simple tally logic...
    }
}
