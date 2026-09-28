package com.dogfood.judging.normalization;

import com.dogfood.judging.entity.*;
import com.dogfood.judging.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core normalization engine implementing per-judge z-score normalization with Bayesian shrinkage.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>For each judge j and criterion c, compute μ_jc (mean) and σ_jc (sample std dev)</li>
 *   <li>z_jc(s) = (raw_score - μ_jc) / σ_jc  (handle σ=0 → z=0)</li>
 *   <li>Apply shrinkage: adjusted_z = (k/(k+k₀)) × z + (k₀/(k+k₀)) × global_mean_z</li>
 *   <li>Final weighted score: F(s) = (1/|J(s)|) × Σ_j Σ_c (w_c × adjusted_z_jc(s))</li>
 *   <li>Display score: clamp(75 + 15 × F(s), 0, 100)</li>
 * </ol>
 *
 * Triggered asynchronously via RabbitMQ when a score.submitted event is received.
 * The recomputation is idempotent — running it multiple times on the same data
 * produces identical results.
 *
 * @see com.dogfood.judging.messaging.ScoreEventConsumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NormalizationEngine {

    /** Shrinkage hyperparameter: pseudo-observations count. */
    private static final int K0 = 5;

    /** Display score mean target. */
    private static final double DISPLAY_MEAN = 75.0;

    /** Display score standard deviation target. */
    private static final double DISPLAY_SIGMA = 15.0;

    private final ScoreRepository scoreRepository;
    private final NormalizedScoreRepository normalizedScoreRepository;
    private final FinalScoreRepository finalScoreRepository;
    private final com.dogfood.judging.repository.RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;
    private final RubricRepository rubricRepository;

    /**
     * Full recomputation of normalized and final scores for an event.
     * This is the main entry point, called asynchronously after each score submission.
     */
    
    @Transactional
    public void recompute(UUID eventId) {
        log.info("Starting scoring recompute for event={}", eventId);

        List<Score> allScores = scoreRepository.findByEventId(eventId);
        if (allScores.isEmpty()) {
            log.info("No scores found for event={}. Skipping.", eventId);
            return;
        }

        List<Criterion> criteria = criterionRepository.findByEventId(eventId);
        if (criteria.isEmpty()) {
            log.warn("No criteria found for event={}. Cannot compute scores.", eventId);
            return;
        }

        boolean normalizationEnabled = rubricRepository.findByEventId(eventId)
                .map(com.dogfood.judging.entity.Rubric::getNormalizationEnabled)
                .orElse(true);

        if (normalizationEnabled) {
            Map<UUID, Map<UUID, List<Score>>> scoresByJudgeByCriterion = allScores.stream()
                    .collect(Collectors.groupingBy(Score::getJudgeId,
                            Collectors.groupingBy(Score::getCriterionId)));

            List<NormalizedScore> allNormalized = new ArrayList<>();
            for (Map.Entry<UUID, Map<UUID, List<Score>>> judgeEntry : scoresByJudgeByCriterion.entrySet()) {
                UUID judgeId = judgeEntry.getKey();
                for (Map.Entry<UUID, List<Score>> critEntry : judgeEntry.getValue().entrySet()) {
                    UUID criterionId = critEntry.getKey();
                    List<Score> scores = critEntry.getValue();
                    List<NormalizedScore> normalized = computeZScores(eventId, judgeId, criterionId, scores);
                    allNormalized.addAll(normalized);
                }
            }

            applyShrinkage(allNormalized, scoresByJudgeByCriterion);
            
            // Save normalized scores
            normalizedScoreRepository.deleteByEventId(eventId);
            normalizedScoreRepository.saveAll(allNormalized);
            
            computeFinalScores(eventId, criteria, allNormalized);
        } else {
            computeRawFinalScores(eventId, criteria, allScores);
        }
    }

    private void computeRawFinalScores(UUID eventId, List<Criterion> criteria, List<Score> allScores) {
        Map<UUID, Double> weightMap = criteria.stream()
                .collect(Collectors.toMap(Criterion::getId, c -> c.getWeight().doubleValue()));

        Map<UUID, List<Score>> bySubmission = allScores.stream()
                .collect(Collectors.groupingBy(Score::getSubmissionId));

        List<FinalScore> finalScores = new ArrayList<>();

        for (Map.Entry<UUID, List<Score>> entry : bySubmission.entrySet()) {
            UUID submissionId = entry.getKey();
            List<Score> submissionScores = entry.getValue();

            Set<UUID> judges = submissionScores.stream()
                    .map(Score::getJudgeId)
                    .collect(Collectors.toSet());
            int judgeCount = judges.size();
            if (judgeCount == 0) continue;

            Map<UUID, Double> judgeComposites = new HashMap<>();
            for (Score s : submissionScores) {
                Double weight = weightMap.getOrDefault(s.getCriterionId(), 0.0);
                double contribution = weight * s.getRawScore();
                judgeComposites.merge(s.getJudgeId(), contribution, Double::sum);
            }

            double finalWeightedScore = judgeComposites.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Raw scores are 1-10. Display score is 10-100.
            double displayScore = Math.max(0, Math.min(100, finalWeightedScore * 10));

            FinalScore fs = new FinalScore();
            fs.setEventId(eventId);
            fs.setSubmissionId(submissionId);
            fs.setWeightedScore(java.math.BigDecimal.valueOf(finalWeightedScore).setScale(6, java.math.RoundingMode.HALF_UP));
            fs.setDisplayScore(java.math.BigDecimal.valueOf(displayScore).setScale(4, java.math.RoundingMode.HALF_UP));
            fs.setJudgeCount(judgeCount);
            fs.setComputedAt(ZonedDateTime.now());

            finalScores.add(fs);
        }

        finalScores.sort((a, b) -> b.getWeightedScore().compareTo(a.getWeightedScore()));
        for (int i = 0; i < finalScores.size(); i++) {
            finalScores.get(i).setRank(i + 1);
        }

        for (FinalScore fs : finalScores) {
            finalScoreRepository.findByEventIdAndSubmissionId(fs.getEventId(), fs.getSubmissionId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setWeightedScore(fs.getWeightedScore());
                                existing.setDisplayScore(fs.getDisplayScore());
                                existing.setRank(fs.getRank());
                                existing.setJudgeCount(fs.getJudgeCount());
                                existing.setComputedAt(ZonedDateTime.now());
                                finalScoreRepository.save(existing);
                            },
                            () -> finalScoreRepository.save(fs)
                    );
        }
        log.info("Computed raw final scores for {} submissions in event={}", finalScores.size(), eventId);
    }
private List<NormalizedScore> computeZScoresForJudgeCriterion(
            UUID eventId, UUID judgeId, UUID criterionId, List<Score> scores) {

        int k = scores.size();

        // Edge case: single score — cannot compute meaningful z-score
        if (k < 2) {
            return scores.stream()
                    .map(s -> buildNormalizedScore(eventId, judgeId, s.getSubmissionId(),
                            criterionId, BigDecimal.ZERO, BigDecimal.ZERO, k))
                    .collect(Collectors.toList());
        }

        // Compute mean
        double sum = scores.stream().mapToDouble(Score::getRawScore).sum();
        double mean = sum / k;

        // Compute sample standard deviation (Bessel's correction: divide by k-1)
        double sumSquaredDev = scores.stream()
                .mapToDouble(s -> Math.pow(s.getRawScore() - mean, 2))
                .sum();
        double variance = sumSquaredDev / (k - 1);
        double stdDev = Math.sqrt(variance);

        // Edge case: σ = 0 — judge gave identical scores, no discriminating information
        if (stdDev < 1e-10) {
            log.debug("Judge {} has σ=0 for criterion {}, setting all z=0", judgeId, criterionId);
            return scores.stream()
                    .map(s -> buildNormalizedScore(eventId, judgeId, s.getSubmissionId(),
                            criterionId, BigDecimal.ZERO, BigDecimal.ZERO, k))
                    .collect(Collectors.toList());
        }

        // Compute z-scores
        return scores.stream()
                .map(s -> {
                    double z = (s.getRawScore() - mean) / stdDev;
                    BigDecimal zBd = BigDecimal.valueOf(z).setScale(6, RoundingMode.HALF_UP);
                    return buildNormalizedScore(eventId, judgeId, s.getSubmissionId(),
                            criterionId, zBd, zBd, k); // shrinkage applied later
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply Bayesian shrinkage to pull unreliable scores toward the global mean.
     *
     * adjusted_z = λ(k) × z + (1 - λ(k)) × global_mean_z
     * where λ(k) = k / (k + k₀)
     *
     * With k₀ = 5:
     * - Judge with 20 reviews: λ = 0.80 (mostly trusts judge's scores)
     * - Judge with 2 reviews:  λ = 0.29 (heavily pulls toward global mean)
     */
    private void applyShrinkage(List<NormalizedScore> allNormalized,
                                 Map<UUID, Map<UUID, List<Score>>> scoresByJudgeByCriterion) {

        // Global mean z-score is approximately 0 by construction (mean of z-scores is 0)
        // but we compute it explicitly for correctness
        double globalMeanZ = allNormalized.stream()
                .mapToDouble(ns -> ns.getZScore().doubleValue())
                .average()
                .orElse(0.0);

        for (NormalizedScore ns : allNormalized) {
            int k = ns.getJudgeReviewCount();
            double lambda = (double) k / (k + K0);
            double z = ns.getZScore().doubleValue();

            double adjustedZ = lambda * z + (1 - lambda) * globalMeanZ;
            ns.setShrinkageAdjustedZ(BigDecimal.valueOf(adjustedZ).setScale(6, RoundingMode.HALF_UP));
        }
    }

    /**
     * Compute final weighted scores per submission.
     *
     * F(s) = (1/|J(s)|) × Σ_j Σ_c (w_c × adjusted_z_jc(s))
     * Display score = clamp(75 + 15 × F(s), 0, 100)
     */
    private void computeFinalScores(UUID eventId, List<Criterion> criteria,
                                     List<NormalizedScore> allNormalized) {

        // Build weight map: criterionId -> weight
        Map<UUID, Double> weightMap = criteria.stream()
                .collect(Collectors.toMap(Criterion::getId,
                        c -> c.getWeight().doubleValue()));

        // Group normalized scores by submissionId
        Map<UUID, List<NormalizedScore>> bySubmission = allNormalized.stream()
                .collect(Collectors.groupingBy(NormalizedScore::getSubmissionId));

        List<FinalScore> finalScores = new ArrayList<>();

        for (Map.Entry<UUID, List<NormalizedScore>> entry : bySubmission.entrySet()) {
            UUID submissionId = entry.getKey();
            List<NormalizedScore> submissionNormalized = entry.getValue();

            // Count distinct judges who scored this submission
            Set<UUID> judges = submissionNormalized.stream()
                    .map(NormalizedScore::getJudgeId)
                    .collect(Collectors.toSet());
            int judgeCount = judges.size();

            if (judgeCount == 0) continue;

            // Compute weighted sum per judge, then average across judges
            Map<UUID, Double> judgeComposites = new HashMap<>();
            for (NormalizedScore ns : submissionNormalized) {
                Double weight = weightMap.getOrDefault(ns.getCriterionId(), 0.0);
                double contribution = weight * ns.getShrinkageAdjustedZ().doubleValue();
                judgeComposites.merge(ns.getJudgeId(), contribution, Double::sum);
            }

            double finalWeightedScore = judgeComposites.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Convert to display score: clamp(75 + 15 × F(s), 0, 100)
            double displayScore = Math.max(0, Math.min(100,
                    DISPLAY_MEAN + DISPLAY_SIGMA * finalWeightedScore));

            FinalScore fs = new FinalScore();
            fs.setEventId(eventId);
            fs.setSubmissionId(submissionId);
            fs.setWeightedScore(BigDecimal.valueOf(finalWeightedScore).setScale(6, RoundingMode.HALF_UP));
            fs.setDisplayScore(BigDecimal.valueOf(displayScore).setScale(4, RoundingMode.HALF_UP));
            fs.setJudgeCount(judgeCount);
            fs.setComputedAt(ZonedDateTime.now());

            finalScores.add(fs);
        }

        // Sort and assign ranks
        finalScores.sort((a, b) -> b.getWeightedScore().compareTo(a.getWeightedScore()));
        for (int i = 0; i < finalScores.size(); i++) {
            finalScores.get(i).setRank(i + 1);
        }

        // Persist (upsert by eventId + submissionId)
        for (FinalScore fs : finalScores) {
            finalScoreRepository.findByEventIdAndSubmissionId(fs.getEventId(), fs.getSubmissionId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setWeightedScore(fs.getWeightedScore());
                                existing.setDisplayScore(fs.getDisplayScore());
                                existing.setRank(fs.getRank());
                                existing.setJudgeCount(fs.getJudgeCount());
                                existing.setComputedAt(ZonedDateTime.now());
                                finalScoreRepository.save(existing);
                            },
                            () -> finalScoreRepository.save(fs)
                    );
        }

        log.info("Computed final scores for {} submissions in event={}", finalScores.size(), eventId);
    }

    private NormalizedScore buildNormalizedScore(UUID eventId, UUID judgeId, UUID submissionId,
                                                  UUID criterionId, BigDecimal zScore,
                                                  BigDecimal adjustedZ, int reviewCount) {
        NormalizedScore ns = new NormalizedScore();
        ns.setEventId(eventId);
        ns.setJudgeId(judgeId);
        ns.setSubmissionId(submissionId);
        ns.setCriterionId(criterionId);
        ns.setZScore(zScore);
        ns.setShrinkageAdjustedZ(adjustedZ);
        ns.setJudgeReviewCount(reviewCount);
        ns.setComputedAt(ZonedDateTime.now());
        return ns;
    }

    /**
     * Helper method for testing the core normalization math.
     * Computes display score from raw score using z-score normalization with Bayesian shrinkage.
     *
     * @param rawScore The judge's raw score (1-10)
     * @param judgeMean The judge's historical mean score
     * @param judgeStdDev The judge's historical standard deviation
     * @param reviewCount Number of reviews this judge has completed
     * @return Display score (0-100) after z-score + shrinkage + scaling
     */
    public double calculateFinalScore(double rawScore, double judgeMean, double judgeStdDev, int reviewCount) {
        // Handle edge cases
        if (reviewCount < 2 || judgeStdDev < 1e-10) {
            return DISPLAY_MEAN; // Return global mean (75.0) if insufficient data
        }

        // 1. Compute z-score: (raw - mean) / stdDev
        double zScore = (rawScore - judgeMean) / judgeStdDev;

        // 2. Apply Bayesian shrinkage with k₀ = 5
        double lambda = (double) reviewCount / (reviewCount + K0);
        double shrinkageZ = lambda * zScore; // global mean z = 0

        // 3. Convert to display score: clamp(75 + 15 × z, 0, 100)
        double displayScore = DISPLAY_MEAN + DISPLAY_SIGMA * shrinkageZ;
        return Math.max(0, Math.min(100, displayScore));
    }
}
