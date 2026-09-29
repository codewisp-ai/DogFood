package com.dogfood.judging.controller;

import com.dogfood.common.dto.ApiError;
import com.dogfood.common.security.RequestContext;
import com.dogfood.judging.entity.*;
import com.dogfood.judging.repository.*;
import com.dogfood.judging.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Scoring endpoints for judges.
 * Role isolation: every query is filtered by judge_id extracted from the JWT
 * (via X-User-Id header), NEVER from a request parameter.
 * Defense-in-depth: Postgres RLS policies also enforce this at the DB level.
 */
@Slf4j
@RestController
@RequestMapping("/api/judging")
@RequiredArgsConstructor
@Tag(name = "Scoring", description = "Judge scoring endpoints with role isolation")
public class ScoringController {

    private final ScoringService scoringService;
    private final JudgeAssignmentRepository assignmentRepository;
    private final ScoreRepository scoreRepository;

    @Operation(summary = "Get my assigned submissions", description = "Returns only submissions assigned to the authenticated judge")
    @GetMapping("/my-assignments")
    public ResponseEntity<List<JudgeAssignment>> getMyAssignments(HttpServletRequest request) {
        UUID judgeId = RequestContext.getUserId(request);
        if (judgeId == null || !RequestContext.isJudge(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<JudgeAssignment> assignments = assignmentRepository.findByJudgeId(judgeId);
        return ResponseEntity.ok(assignments);
    }

    @Operation(summary = "Submit a score", description = "Submit or update a score. Requires Idempotency-Key header to prevent duplicate submissions.")
    @PostMapping("/scores")
    public ResponseEntity<?> submitScore(
            @RequestBody ScoreRequest request,
            HttpServletRequest httpRequest) {

        UUID judgeId = RequestContext.getUserId(httpRequest);
        if (judgeId == null || !RequestContext.isJudge(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError(403, "Forbidden", "Judge role required", "/api/judging/scores"));
        }

        String idempotencyKey = RequestContext.getIdempotencyKey(httpRequest);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiError(400, "Bad Request", "Idempotency-Key header is required", "/api/judging/scores"));
        }

        try {
            Score score = scoringService.submitScore(
                    request.eventId(), judgeId, request.submissionId(),
                    request.criterionId(), request.rawScore(), request.feedback(), idempotencyKey,
                    RequestContext.getCorrelationId(httpRequest));
            return ResponseEntity.ok(score);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiError(400, "Bad Request", e.getMessage(), "/api/judging/scores"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError(409, "Conflict", e.getMessage(), "/api/judging/scores"));
        }
    }

    @Operation(summary = "Get my scores for a submission", description = "Returns only the authenticated judge's scores")
    @GetMapping("/scores/{submissionId}")
    public ResponseEntity<?> getMyScores(
        @PathVariable UUID submissionId,
        HttpServletRequest request) {
        UUID judgeId = RequestContext.getUserId(request);
        if (judgeId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Role isolation: query filtered by judgeId from JWT, NEVER from request params
        List<Score> scores = scoreRepository.findByJudgeIdAndSubmissionId(judgeId, submissionId);
        return ResponseEntity.ok(scores);
    }

    @Operation(summary = "Get scores for a specific judge", description = "Fails with 403 if attempting to read a peer's scores")
    @GetMapping("/scores")
    public ResponseEntity<?> getScoresByJudge(
            @RequestParam("judgeId") UUID requestedJudgeId,
            HttpServletRequest request) {
        UUID myId = RequestContext.getUserId(request);
        if (myId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!myId.equals(requestedJudgeId) && !RequestContext.isOrganizer(request)) {
            // Strictly enforce that judges cannot see peer scores
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError(403, "Forbidden", "Cannot view peer scores", "/api/judging/scores"));
        }
        List<Score> scores = scoreRepository.findByJudgeId(requestedJudgeId);
        return ResponseEntity.ok(scores);
    }

    /**
     * Score submission request body.
     */
    public record ScoreRequest(
            UUID eventId,
            UUID submissionId,
            UUID criterionId,
            int rawScore,
            String feedback
    ) {}
}
