package com.dogfood.voting.strategy;

import com.dogfood.voting.dto.VoteRequest;
import com.dogfood.voting.entity.Vote;
import java.util.UUID;

public interface VotingStrategy {
    Vote castVote(UUID eventId, UUID voterId, String voterIp, VoteRequest request);
    void tallyVotes(UUID eventId);
}
