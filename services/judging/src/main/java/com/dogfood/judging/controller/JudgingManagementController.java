package com.dogfood.judging.controller;

import com.dogfood.common.security.RequestContext;
import com.dogfood.judging.entity.*;
import com.dogfood.judging.normalization.NormalizationEngine;
import com.dogfood.judging.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Organizer-facing endpoints for event results, rubric management,
 * judge assignment, and integrity reporting.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Judging Management", description = "Organizer-facing judging management")
public class JudgingManagementController {

    private final RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;
    private final JudgeAssignmentRepository assignmentRepository;
    private final FinalScoreRepository finalScoreRepository;
    private final ScoreRepository scoreRepository;
    private final NormalizedScoreRepository normalizedScoreRepository;
    private final ConflictOfInterestRepository coiRepository;
    private final NormalizationEngine normalizationEngine;

    // ── Rubric Management ─────────────────────────────────────────

    @Operation(summary = "Create or update scoring rubric for an event")
    @PostMapping("/api/events/{eventId}/rubric")
    public ResponseEntity<?> createRubric(
            @PathVariable UUID eventId,
            @RequestBody RubricRequest request,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validate weights sum to 1.0
        BigDecimal totalWeight = request.criteria().stream()
                .map(CriterionRequest::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ONE) != 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Criteria weights must sum to 1.0, got " + totalWeight));
        }

        // Create or update rubric
        Rubric rubric = rubricRepository.findByEventId(eventId)
                .orElseGet(() -> {
                    Rubric r = new Rubric();
                    r.setEventId(eventId);
                    return rubricRepository.save(r);
                });

        // Delete old criteria and create new
        List<Criterion> existingCriteria = criterionRepository.findByRubricId(rubric.getId());
        criterionRepository.deleteAll(existingCriteria);

        List<Criterion> newCriteria = new ArrayList<>();
        for (int i = 0; i < request.criteria().size(); i++) {
            CriterionRequest cr = request.criteria().get(i);
            Criterion c = new Criterion();
            c.setRubricId(rubric.getId());
            c.setName(cr.name());
            c.setDescription(cr.description());
            c.setWeight(cr.weight());
            c.setMaxScore(cr.maxScore() != null ? cr.maxScore() : 10);
            c.setSortOrder(i);
            newCriteria.add(c);
        }
        criterionRepository.saveAll(newCriteria);

        return ResponseEntity.ok(Map.of("rubricId", rubric.getId(), "criteria", newCriteria));
    }

    @Operation(summary = "Get rubric for an event")
    @GetMapping("/api/events/{eventId}/rubric")
    public ResponseEntity<?> getRubric(@PathVariable UUID eventId) {
        return rubricRepository.findByEventId(eventId)
                .map(rubric -> {
                    List<Criterion> criteria = criterionRepository.findByRubricIdOrderBySortOrderAsc(rubric.getId());
                    return ResponseEntity.ok(Map.of("rubric", rubric, "criteria", criteria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Judge Assignment ──────────────────────────────────────────

    @Operation(summary = "Assign judges to submissions (round-robin, disjoint batches)")
    @PostMapping("/api/events/{eventId}/assign-judges")
    public ResponseEntity<?> assignJudges(
            @PathVariable UUID eventId,
            @RequestBody AssignJudgesRequest request,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UUID> judgeIds = request.judgeIds();
        List<UUID> submissionIds = request.submissionIds();
        int reviewsPerSubmission = request.reviewsPerSubmission() != null ? request.reviewsPerSubmission() : 3;

        // Load COI exclusions
        List<ConflictOfInterest> cois = coiRepository.findByEventId(eventId);
        Map<UUID, Set<UUID>> coiMap = cois.stream()
                .collect(Collectors.groupingBy(ConflictOfInterest::getJudgeId,
                        Collectors.mapping(ConflictOfInterest::getSubmissionId, Collectors.toSet())));

        // Round-robin assignment with COI exclusion
        List<JudgeAssignment> assignments = new ArrayList<>();
        UUID batchId = UUID.randomUUID();

        for (int r = 0; r < reviewsPerSubmission; r++) {
            for (int s = 0; s < submissionIds.size(); s++) {
                UUID submissionId = submissionIds.get(s);
                // Pick a judge who hasn't been assigned this submission and has no COI
                for (int attempt = 0; attempt < judgeIds.size(); attempt++) {
                    int judgeIdx = (s + r + attempt) % judgeIds.size();
                    UUID judgeId = judgeIds.get(judgeIdx);

                    // Check COI
                    Set<UUID> judgeCoiSubmissions = coiMap.getOrDefault(judgeId, Set.of());
                    if (judgeCoiSubmissions.contains(submissionId)) continue;

                    // Check not already assigned
                    boolean alreadyAssigned = assignments.stream()
                            .anyMatch(a -> a.getJudgeId().equals(judgeId) && a.getSubmissionId().equals(submissionId));
                    if (alreadyAssigned) continue;

                    JudgeAssignment assignment = new JudgeAssignment();
                    assignment.setEventId(eventId);
                    assignment.setJudgeId(judgeId);
                    assignment.setSubmissionId(submissionId);
                    assignment.setBatchId(batchId);
                    assignment.setTrackId(request.trackId());
                    assignment.setStatus("PENDING");
                    assignments.add(assignment);
                    break;
                }
            }
        }

        assignmentRepository.saveAll(assignments);
        return ResponseEntity.ok(Map.of("assigned", assignments.size(), "batchId", batchId));
    }

    // ── Results ───────────────────────────────────────────────────

    @Operation(summary = "Get ranked results for an event (organizer only)")
    @GetMapping("/api/events/{eventId}/results")
    public ResponseEntity<?> getResults(
            @PathVariable UUID eventId,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FinalScore> results = finalScoreRepository.findByEventIdOrderByRankAsc(eventId);
        return ResponseEntity.ok(results);
    }

    // ── Integrity Report ──────────────────────────────────────────

    @Operation(summary = "Generate judging integrity report", description = "Shows raw vs normalized scores, rank movements, judge deviation analysis")
    @GetMapping("/api/events/{eventId}/integrity-report")
    public ResponseEntity<?> getIntegrityReport(
            @PathVariable UUID eventId,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Score> rawScores = scoreRepository.findByEventId(eventId);
        List<NormalizedScore> normalizedScores = normalizedScoreRepository.findByEventId(eventId);
        List<FinalScore> finalScores = finalScoreRepository.findByEventIdOrderByRankAsc(eventId);

        // Build per-judge deviation analysis
        Map<UUID, JudgeStats> judgeStats = new HashMap<>();
        Map<UUID, List<NormalizedScore>> byJudge = normalizedScores.stream()
                .collect(Collectors.groupingBy(NormalizedScore::getJudgeId));

        for (Map.Entry<UUID, List<NormalizedScore>> entry : byJudge.entrySet()) {
            UUID judgeId = entry.getKey();
            List<NormalizedScore> judgeNormalized = entry.getValue();
            double avgAbsZ = judgeNormalized.stream()
                    .mapToDouble(ns -> Math.abs(ns.getShrinkageAdjustedZ().doubleValue()))
                    .average().orElse(0);
            int reviewCount = judgeNormalized.size();
            judgeStats.put(judgeId, new JudgeStats(judgeId, reviewCount, avgAbsZ));
        }

        return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "normalizationMethod", "Per-judge z-score with Bayesian shrinkage (k₀=5)",
                "rawScoreCount", rawScores.size(),
                "normalizedScoreCount", normalizedScores.size(),
                "finalRankings", finalScores,
                "judgeStatistics", judgeStats.values()
        ));
    }

    // ── COI ───────────────────────────────────────────────────────

    @Operation(summary = "Declare conflict of interest")
    @PostMapping("/api/judging/coi")
    public ResponseEntity<?> declareCoi(
            @RequestBody CoiRequest request,
            HttpServletRequest httpRequest) {
        UUID judgeId = RequestContext.getUserId(httpRequest);
        if (judgeId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (coiRepository.existsByJudgeIdAndSubmissionId(judgeId, request.submissionId())) {
            return ResponseEntity.ok(Map.of("message", "COI already declared"));
        }

        ConflictOfInterest coi = new ConflictOfInterest();
        coi.setEventId(request.eventId());
        coi.setJudgeId(judgeId);
        coi.setSubmissionId(request.submissionId());
        coi.setReason(request.reason());
        coiRepository.save(coi);

        // Recuse from assignment
        assignmentRepository.findByJudgeIdAndSubmissionId(judgeId, request.submissionId())
                .ifPresent(assignment -> {
                    assignment.setStatus("RECUSED");
                    assignmentRepository.save(assignment);
                });

        return ResponseEntity.ok(coi);
    }

    @GetMapping("/api/judging/coi")
    public ResponseEntity<?> getMyCois(HttpServletRequest request) {
        UUID judgeId = RequestContext.getUserId(request);
        if (judgeId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(coiRepository.findByJudgeId(judgeId));
    }

    @Operation(summary = "Bulk export event results")
    @GetMapping(value = "/api/events/{eventId}/results/export", produces = "text/csv")
    public ResponseEntity<String> exportResults(
            @PathVariable UUID eventId,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<JudgeAssignment> assignments = assignmentRepository.findByEventId(eventId);
        List<Score> scores = scoreRepository.findByEventId(eventId);
        List<FinalScore> finalScores = finalScoreRepository.findByEventIdOrderByRankAsc(eventId);

        StringBuilder csv = new StringBuilder();
        csv.append("SubmissionId,JudgeId,AssignmentStatus,CriterionId,RawScore,FinalScore,Rank\n");

        Map<UUID, FinalScore> finalScoreMap = finalScores.stream()
                .collect(Collectors.toMap(FinalScore::getSubmissionId, fs -> fs, (f1, f2) -> f1));

        Map<String, List<Score>> scoresByAssignment = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getJudgeId().toString() + "_" + s.getSubmissionId().toString()));

        for (JudgeAssignment assignment : assignments) {
            String key = assignment.getJudgeId().toString() + "_" + assignment.getSubmissionId().toString();
            List<Score> assignmentScores = scoresByAssignment.getOrDefault(key, Collections.emptyList());
            
            FinalScore fs = finalScoreMap.get(assignment.getSubmissionId());
            String finalScoreStr = fs != null && fs.getDisplayScore() != null ? fs.getDisplayScore().toString() : "";
            String rankStr = fs != null && fs.getRank() != null ? fs.getRank().toString() : "";

            if (assignmentScores.isEmpty()) {
                csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        assignment.getSubmissionId(),
                        assignment.getJudgeId(),
                        assignment.getStatus() != null ? assignment.getStatus() : "",
                        "",
                        "",
                        finalScoreStr,
                        rankStr
                ));
            } else {
                for (Score s : assignmentScores) {
                    csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                            assignment.getSubmissionId(),
                            assignment.getJudgeId(),
                            assignment.getStatus() != null ? assignment.getStatus() : "",
                            s.getCriterionId() != null ? s.getCriterionId() : "",
                            s.getRawScore() != null ? s.getRawScore() : "",
                            finalScoreStr,
                            rankStr
                    ));
                }
            }
        }

        return ResponseEntity.ok(csv.toString());
    }

    // ── DTOs ──────────────────────────────────────────────────────

    public record RubricRequest(List<CriterionRequest> criteria) {}
    public record CriterionRequest(String name, String description, BigDecimal weight, Integer maxScore) {}
    public record AssignJudgesRequest(List<UUID> judgeIds, List<UUID> submissionIds, Integer reviewsPerSubmission, UUID trackId) {}
    public record CoiRequest(UUID eventId, UUID submissionId, String reason) {}
    record JudgeStats(UUID judgeId, int reviewCount, double avgAbsoluteZScore) {}
}
