package com.dogfood.judging.controller;

import com.dogfood.common.security.RequestContext;
import com.dogfood.judging.entity.*;
import com.dogfood.judging.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Calibration round and SSE judge-progress endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Judging Calibration")
public class CalibrationProgressController {

    private final CalibrationSubmissionRepository calibSubmissionRepo;
    private final CalibrationReferenceScoreRepository refScoreRepo;
    private final CalibrationResultRepository calibResultRepo;
    private final JudgeAssignmentRepository assignmentRepo;
    private final SubmissionFlagRepository submissionFlagRepo;

    private final List<SseEmitter> progressEmitters = new CopyOnWriteArrayList<>();

    // -- Calibration --

    @Operation(summary = "Start calibration round", description = "Organizer selects reference submissions and sets expected scores")
    @PostMapping("/api/events/{eventId}/calibration/start")
    public ResponseEntity<?> startCalibration(
            @PathVariable UUID eventId,
            @RequestBody CalibrationStartRequest request,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CalibrationSubmission> calibSubs = new ArrayList<>();
        for (CalibrationStartRequest.RefSubmission ref : request.referenceSubmissions()) {
            CalibrationSubmission cs = new CalibrationSubmission();
            cs.setEventId(eventId);
            cs.setSubmissionId(ref.submissionId());
            cs = calibSubmissionRepo.save(cs);

            for (CalibrationStartRequest.RefScore score : ref.referenceScores()) {
                CalibrationReferenceScore crs = new CalibrationReferenceScore();
                crs.setCalibrationSubmissionId(cs.getId());
                crs.setCriterionId(score.criterionId());
                crs.setReferenceScore(score.score());
                refScoreRepo.save(crs);
            }
            calibSubs.add(cs);
        }

        return ResponseEntity.ok(Map.of("calibrationSubmissions", calibSubs.size()));
    }

    @Operation(summary = "Submit calibration score", description = "Judge submits scores for a calibration reference submission")
    @PostMapping("/api/judging/calibration/scores")
    public ResponseEntity<?> submitCalibrationScore(
            @RequestBody CalibrationScoreRequest request,
            HttpServletRequest httpRequest) {

        UUID judgeId = RequestContext.getUserId(httpRequest);
        if (judgeId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<CalibrationReferenceScore> refScores = refScoreRepo
                .findByCalibrationSubmissionId(request.calibrationSubmissionId());

        CalibrationReferenceScore ref = refScores.stream()
                .filter(r -> r.getCriterionId().equals(request.criterionId()))
                .findFirst().orElse(null);

        int referenceScore = ref != null ? ref.getReferenceScore() : 0;
        int deviation = Math.abs(request.score() - referenceScore);

        CalibrationResult result = new CalibrationResult();
        result.setEventId(request.eventId());
        result.setJudgeId(judgeId);
        result.setCalibrationSubmissionId(request.calibrationSubmissionId());
        result.setCriterionId(request.criterionId());
        result.setJudgeScore(request.score());
        result.setReferenceScore(referenceScore);
        result.setDeviation(deviation);
        calibResultRepo.save(result);

        boolean miscalibrated = deviation > 2;
        return ResponseEntity.ok(Map.of(
                "deviation", deviation,
                "miscalibrated", miscalibrated,
                "message", miscalibrated ? "Score deviates significantly from reference" : "Within acceptable range"
        ));
    }

    @Operation(summary = "Get calibration report for an event")
    @GetMapping("/api/events/{eventId}/calibration/report")
    public ResponseEntity<?> getCalibrationReport(
            @PathVariable UUID eventId,
            HttpServletRequest httpRequest) {

        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CalibrationResult> results = calibResultRepo.findByEventId(eventId);
        Map<UUID, List<CalibrationResult>> byJudge = results.stream()
                .collect(Collectors.groupingBy(CalibrationResult::getJudgeId));

        List<Map<String, Object>> judgeReports = new ArrayList<>();
        for (Map.Entry<UUID, List<CalibrationResult>> entry : byJudge.entrySet()) {
            double avgDeviation = entry.getValue().stream()
                    .mapToInt(CalibrationResult::getDeviation)
                    .average().orElse(0);
            boolean miscalibrated = avgDeviation > 2.0;
            judgeReports.add(Map.of(
                    "judgeId", entry.getKey(),
                    "avgDeviation", avgDeviation,
                    "miscalibrated", miscalibrated,
                    "scores", entry.getValue()
            ));
        }

        return ResponseEntity.ok(Map.of("eventId", eventId, "judges", judgeReports));
    }

    // -- SSE Judge Progress --

    @Operation(summary = "Live judge progress stream (SSE)", description = "Real-time updates on which judges have started/completed scoring")
    @GetMapping(value = "/api/events/{eventId}/judge-progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamJudgeProgress(@PathVariable UUID eventId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        progressEmitters.add(emitter);

        emitter.onCompletion(() -> progressEmitters.remove(emitter));
        emitter.onTimeout(() -> progressEmitters.remove(emitter));
        emitter.onError(e -> progressEmitters.remove(emitter));

        try {
            List<JudgeAssignment> assignments = assignmentRepo.findByEventId(eventId);
            Map<String, Long> statusCounts = assignments.stream()
                    .collect(Collectors.groupingBy(JudgeAssignment::getStatus, Collectors.counting()));
            emitter.send(SseEmitter.event()
                    .name("SNAPSHOT")
                    .data(Map.of("eventId", eventId, "statusCounts", statusCounts,
                            "totalAssignments", assignments.size())));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // -- Flag / Abstain --

    @Operation(summary = "Flag a submission as broken/ineligible")
    @PostMapping("/api/judging/flag")
    public ResponseEntity<?> flagSubmission(
            @RequestBody FlagRequest request,
            HttpServletRequest httpRequest) {
        UUID judgeId = RequestContext.getUserId(httpRequest);
        if (judgeId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // Check if already flagged by this judge
        if (submissionFlagRepo.existsByJudgeIdAndSubmissionId(judgeId, request.submissionId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You have already flagged this submission"));
        }

        SubmissionFlag flag = new SubmissionFlag();
        flag.setEventId(request.eventId());
        flag.setJudgeId(judgeId);
        flag.setSubmissionId(request.submissionId());
        flag.setReason(request.reason());
        flag.setStatus("PENDING");
        flag.setFlaggedAt(ZonedDateTime.now());

        // Actually save to database!
        SubmissionFlag savedFlag = submissionFlagRepo.save(flag);

        return ResponseEntity.ok(Map.of(
                "message", "Submission flagged for organizer review",
                "flagId", savedFlag.getId(),
                "status", savedFlag.getStatus()
        ));
    }
    
    @Operation(summary = "Get all flagged submissions for an event")
    @GetMapping("/api/events/{eventId}/flags")
    public ResponseEntity<?> getFlaggedSubmissions(@PathVariable UUID eventId, HttpServletRequest httpRequest) {
        if (!RequestContext.isOrganizer(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<SubmissionFlag> flags = submissionFlagRepo.findByEventId(eventId);
        return ResponseEntity.ok(Map.of("flags", flags, "count", flags.size()));
    }

    // -- Request DTOs --

    public record CalibrationStartRequest(List<RefSubmission> referenceSubmissions) {
        public record RefSubmission(UUID submissionId, List<RefScore> referenceScores) {}
        public record RefScore(UUID criterionId, int score) {}
    }

    public record CalibrationScoreRequest(UUID eventId, UUID calibrationSubmissionId, UUID criterionId, int score) {}
    public record FlagRequest(UUID eventId, UUID submissionId, String reason) {}
}
