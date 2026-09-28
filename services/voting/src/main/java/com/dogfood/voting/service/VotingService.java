package com.dogfood.voting.service;

import com.dogfood.voting.abuse.DuplicateDetector;
import com.dogfood.voting.abuse.RateLimiter;
import com.dogfood.voting.dto.VoteRequest;
import com.dogfood.voting.entity.Vote;
import com.dogfood.voting.entity.VoteResult;
import com.dogfood.voting.repository.VoteRepository;
import com.dogfood.voting.repository.VoteResultRepository;
import com.dogfood.voting.strategy.VotingStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VotingService {
    private final Map<String, VotingStrategy> strategies;
    private final RateLimiter rateLimiter;
    private final DuplicateDetector duplicateDetector;
    private final RabbitTemplate rabbitTemplate;
    private final VoteRepository voteRepository;
    private final VoteResultRepository voteResultRepository;

    public VotingService(Map<String, VotingStrategy> strategies, RateLimiter rateLimiter, DuplicateDetector duplicateDetector, RabbitTemplate rabbitTemplate, VoteRepository voteRepository, VoteResultRepository voteResultRepository) {
        this.strategies = strategies;
        this.rateLimiter = rateLimiter;
        this.duplicateDetector = duplicateDetector;
        this.rabbitTemplate = rabbitTemplate;
        this.voteRepository = voteRepository;
        this.voteResultRepository = voteResultRepository;
    }

    public Vote castVote(UUID eventId, UUID voterId, String voterIp, VoteRequest request, String strategyName) {
        if (voterIp != null && !rateLimiter.isAllowed(voterIp, 10, Duration.ofMinutes(1))) {
            com.dogfood.common.events.AuditEvent abuseEvent = com.dogfood.common.events.AuditEvent.of(
                "vote.abuse_detected", "vote", null, voterId, "VOTER",
                com.dogfood.common.security.RequestContext.getCorrelationId(), Map.of("voterIp", voterIp)
            );
            rabbitTemplate.convertAndSend("dogfood.audit", "vote.abuse_detected", abuseEvent);
            throw new RuntimeException("Rate limit exceeded for IP");
        }
        if (voterId != null && !rateLimiter.isAllowed(voterId.toString(), 20, Duration.ofMinutes(1))) {
            com.dogfood.common.events.AuditEvent abuseEvent = com.dogfood.common.events.AuditEvent.of(
                "vote.abuse_detected", "vote", null, voterId, "VOTER",
                com.dogfood.common.security.RequestContext.getCorrelationId(), Map.of("voterId", voterId)
            );
            rabbitTemplate.convertAndSend("dogfood.audit", "vote.abuse_detected", abuseEvent);
            throw new RuntimeException("Rate limit exceeded for user");
        }
        if (voterId != null && duplicateDetector.isDuplicate(eventId, request.submissionId(), voterId)) {
            com.dogfood.common.events.AuditEvent abuseEvent = com.dogfood.common.events.AuditEvent.of(
                "vote.abuse_detected", "vote", null, voterId, "VOTER",
                com.dogfood.common.security.RequestContext.getCorrelationId(), Map.of("reason", "duplicate")
            );
            rabbitTemplate.convertAndSend("dogfood.audit", "vote.abuse_detected", abuseEvent);
            throw new RuntimeException("Duplicate vote");
        }

        VotingStrategy strategy = strategies.getOrDefault(strategyName + "VotingStrategy", strategies.get("simpleVotingStrategy"));
        Vote vote = strategy.castVote(eventId, voterId, voterIp, request);
        
        com.dogfood.common.events.AuditEvent auditEvent = com.dogfood.common.events.AuditEvent.of(
            "vote.cast",
            "vote",
            vote.getId(),
            voterId,
            "VOTER",
            com.dogfood.common.security.RequestContext.getCorrelationId(),
            Map.of("voteId", vote.getId())
        );
        rabbitTemplate.convertAndSend("dogfood.audit", "vote.cast", auditEvent);
        return vote;
    }

    public List<VoteResult> getResults(UUID eventId, boolean isOrganizer, boolean isVotingClosed) {
        if (!isOrganizer && !isVotingClosed) {
            throw new RuntimeException("Results are hidden until voting is closed");
        }
        return voteResultRepository.findByEventId(eventId);
    }

    public List<Vote> getMyVotes(UUID eventId, UUID voterId) {
        return voteRepository.findByEventIdAndVoterId(eventId, voterId);
    }
}
