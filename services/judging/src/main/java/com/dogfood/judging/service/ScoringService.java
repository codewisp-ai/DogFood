package com.dogfood.judging.service;

import com.dogfood.common.events.ScoreSubmittedEvent;
import com.dogfood.common.events.RabbitConstants;
import com.dogfood.judging.entity.Score;
import com.dogfood.judging.entity.JudgeAssignment;
import com.dogfood.judging.repository.ScoreRepository;
import com.dogfood.judging.repository.JudgeAssignmentRepository;
import com.dogfood.judging.repository.CriterionRepository;
import com.dogfood.judging.security.RlsSessionManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {
    
    private final ScoreRepository scoreRepository;
    private final JudgeAssignmentRepository assignmentRepository;
    private final CriterionRepository criterionRepository;
    private final com.dogfood.judging.repository.RubricRepository rubricRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "judgingService")
    @Transactional
    public Score submitScore(UUID eventId, UUID judgeId, UUID submissionId, UUID criterionId, Integer rawScore, String feedback, String idempotencyKey, String correlationId) {
        log.info("Submitting score for judge={}, submission={}, criterion={}, correlationId={}", 
                judgeId, submissionId, criterionId, correlationId);
        
        // 1. Check idempotency - if we've seen this key before, return existing score
        var existingScore = scoreRepository.findByIdempotencyKey(idempotencyKey);
        if (existingScore.isPresent()) {
            log.info("Score already exists for idempotency key: {}", idempotencyKey);
            return existingScore.get();
        }
        
        // 2. Validate judge is assigned to this submission
        var assignment = assignmentRepository.findByJudgeIdAndSubmissionId(judgeId, submissionId);
        if (assignment.isEmpty()) {
            throw new IllegalArgumentException("Judge is not assigned to this submission");
        }
        
        JudgeAssignment judgeAssignment = assignment.get();
        if ("RECUSED".equals(judgeAssignment.getStatus())) {
            throw new IllegalStateException("Judge has recused from this submission");
        }
        
        // 3. Validate criterion exists (basic check)
        if (!criterionRepository.existsById(criterionId)) {
            throw new IllegalArgumentException("Criterion does not exist: " + criterionId);
        }
        
        // 4. Validate score range (assuming 1-10 based on schema)
        if (rawScore < 1 || rawScore > 10) {
            throw new IllegalArgumentException("Score must be between 1 and 10, got: " + rawScore);
        }
        
        // 5. Create or update the score
        Score score = scoreRepository.findByJudgeIdAndSubmissionIdAndCriterionId(judgeId, submissionId, criterionId)
            .orElse(new Score());
            
        score.setEventId(eventId);
        score.setJudgeId(judgeId);
        score.setSubmissionId(submissionId);
        score.setCriterionId(criterionId);
        score.setRawScore(rawScore);
        if (feedback != null) {
            score.setFeedback(feedback);
        }
        score.setIdempotencyKey(idempotencyKey);
        score.setUpdatedAt(ZonedDateTime.now());
        
        Score savedScore = scoreRepository.save(score);
        
        // 6. Update assignment status: check if all criteria are scored
        judgeAssignment.setStatus("IN_PROGRESS");
        var rubricOpt = rubricRepository.findByEventId(eventId);
        if (rubricOpt.isPresent()) {
            List<Criterion> criteria = criterionRepository.findByRubricId(rubricOpt.get().getId());
            List<Score> scoredList = scoreRepository.findByJudgeIdAndSubmissionId(judgeId, submissionId);
            if (!criteria.isEmpty() && scoredList.size() >= criteria.size()) {
                judgeAssignment.setStatus("COMPLETED");
            }
        }
        assignmentRepository.save(judgeAssignment);
        
        // 7. Publish score submitted event to trigger async normalization
        try {
            ScoreSubmittedEvent event = new ScoreSubmittedEvent(eventId, judgeId, submissionId, criterionId, rawScore, null);
            rabbitTemplate.convertAndSend(RabbitConstants.SCORES_EXCHANGE, RabbitConstants.SCORE_SUBMITTED, event);
            log.info("Published ScoreSubmittedEvent for async normalization");
        } catch (Exception e) {
            log.error("Failed to publish ScoreSubmittedEvent, but score was saved", e);
            // Don't fail the entire operation if event publishing fails
        }
        
        // 8. Publish audit event
        try {
            Map<String, Object> auditDetails = Map.of(
                "judgeId", judgeId.toString(),
                "submissionId", submissionId.toString(),
                "criterionId", criterionId.toString(),
                "rawScore", rawScore,
                "correlationId", correlationId != null ? correlationId : ""
            );
            rabbitTemplate.convertAndSend(RabbitConstants.AUDIT_EXCHANGE, "score.submitted", auditDetails);
        } catch (Exception e) {
            log.error("Failed to publish audit event for score submission", e);
        }
        
        log.info("Successfully submitted score: {}", savedScore.getId());
        return savedScore;
    }
}
