package com.dogfood.voting.controller;

import com.dogfood.voting.dto.VoteRequest;
import com.dogfood.voting.entity.Vote;
import com.dogfood.voting.entity.VoteResult;
import com.dogfood.voting.service.BallotService;
import com.dogfood.voting.service.VotingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/voting")
public class VotingController {
    private final VotingService votingService;
    private final BallotService ballotService;

    public VotingController(VotingService votingService, BallotService ballotService) {
        this.votingService = votingService;
        this.ballotService = ballotService;
    }

    @PostMapping("/{eventId}/vote")
    public Vote castVote(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestParam(defaultValue = "simple") String strategy,
            @RequestBody VoteRequest request) {
        UUID voterId = userId != null ? UUID.fromString(userId) : null;
        return votingService.castVote(eventId, voterId, ip, request, strategy);
    }

    @GetMapping("/{eventId}/ballot")
    public List<UUID> getBallot(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestParam List<UUID> submissionIds) {
        return ballotService.getRandomizedBallot(eventId, UUID.fromString(userId), submissionIds);
    }

    @GetMapping("/{eventId}/my-votes")
    public List<Vote> getMyVotes(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Id") String userId) {
        return votingService.getMyVotes(eventId, UUID.fromString(userId));
    }

    @GetMapping("/{eventId}/results")
    public List<VoteResult> getResults(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestParam(defaultValue = "false") boolean isVotingClosed) {
        boolean isOrganizer = roles != null && roles.contains("ORGANIZER");
        return votingService.getResults(eventId, isOrganizer, isVotingClosed);
    }
}
