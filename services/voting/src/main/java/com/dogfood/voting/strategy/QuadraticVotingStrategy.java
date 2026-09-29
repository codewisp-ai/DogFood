package com.dogfood.voting.strategy;

import com.dogfood.voting.dto.VoteRequest;
import com.dogfood.voting.entity.QuadraticBudget;
import com.dogfood.voting.entity.Vote;
import com.dogfood.voting.repository.QuadraticBudgetRepository;
import com.dogfood.voting.repository.VoteRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component("quadraticVotingStrategy")
public class QuadraticVotingStrategy implements VotingStrategy {
    private final VoteRepository voteRepository;
    private final QuadraticBudgetRepository budgetRepository;

    public QuadraticVotingStrategy(VoteRepository voteRepository, QuadraticBudgetRepository budgetRepository) {
        this.voteRepository = voteRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Vote castVote(UUID eventId, UUID voterId, String voterIp, VoteRequest request) {
        // Input is number of votes to cast, not credits
        int votesToCast = request.votesToCast() != null ? request.votesToCast() : 1;

        QuadraticBudget budget = budgetRepository.findByEventIdAndVoterId(eventId, voterId)
            .orElseGet(() -> {
                QuadraticBudget newBudget = new QuadraticBudget();
                newBudget.setEventId(eventId);
                newBudget.setVoterId(voterId);
                newBudget.setTotalBudget(100);  // 100 credits total
                newBudget.setSpent(0);
                return budgetRepository.save(newBudget);
            });

        // Quadratic cost: n votes cost n^2 credits
        int creditsCost = votesToCast * votesToCast;

        if (budget.getSpent() + creditsCost > budget.getTotalBudget()) {
            throw new RuntimeException("Budget exceeded: need " + creditsCost +
                                     " credits, have " + (budget.getTotalBudget() - budget.getSpent()));
        }

        budget.setSpent(budget.getSpent() + creditsCost);
        budgetRepository.save(budget);

        Vote vote = new Vote();
        vote.setEventId(eventId);
        vote.setSubmissionId(request.submissionId());
        vote.setVoterId(voterId);
        vote.setVoterIp(voterIp);
        vote.setDeviceFingerprint(request.deviceFingerprint());
        vote.setCreditsSpent(creditsCost);              // Credits deducted from budget
        vote.setVoteInfluence(new BigDecimal(votesToCast).setScale(6, RoundingMode.HALF_UP)); // Actual voting power

        return voteRepository.save(vote);
    }

    @Override
    public void tallyVotes(UUID eventId) {
        // Quadratic tally logic...
    }
}
