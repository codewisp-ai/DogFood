package com.dogfood.judging;

import com.dogfood.judging.normalization.NormalizationEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NormalizationEngineTest {

    @Test
    void testZScoreAndBayesianShrinkage() {
        NormalizationEngine engine = new NormalizationEngine(null, null, null, null, null);
        
        // Mock data: Judge gave a raw score of 8.5
        // Judge's historical mean: 7.0, stdDev: 1.5
        // Judge has done 15 reviews (k = 15)
        
        double finalScore = engine.calculateFinalScore(8.5, 7.0, 1.5, 15);
        
        // Z-score = (8.5 - 7.0) / 1.5 = 1.0
        // Bayesian weight lambda = 15 / (15 + 5) = 15 / 20 = 0.75
        // Shrinkage Z = (0.75 * 1.0) + (0.25 * 0) = 0.75
        // Final display = clamp(75 + 15 * 0.75) = clamp(75 + 11.25) = 86.25
        
        assertEquals(86.25, finalScore, 0.01, "Bayesian shrinkage math is incorrect");
    }

    @Test
    void testZeroStandardDeviationEdgeCase() {
        NormalizationEngine engine = new NormalizationEngine(null, null, null, null, null);
        
        // Judge gave identical scores to everyone, so stdDev = 0
        // Raw score: 5.0, mean: 5.0, stdDev: 0.0, k = 10
        
        double finalScore = engine.calculateFinalScore(5.0, 5.0, 0.0, 10);
        
        // Should default to 75 (the global mean)
        assertEquals(75.0, finalScore, 0.01);
    }
}
