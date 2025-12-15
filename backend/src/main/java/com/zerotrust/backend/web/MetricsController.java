package com.zerotrust.backend.web;

import com.zerotrust.backend.entities.RiskScoreHistory;
import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.RiskScoreHistoryRepository;
import com.zerotrust.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metrics and statistics endpoints
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "System Metrics & Statistics APIs")
public class MetricsController {

    private final RiskScoreHistoryRepository historyRepo;
    private final UserRepository userRepo;

    @GetMapping("/system-stats")
    @Operation(summary = "Get System Statistics", description = "Get overall system statistics and metrics")
    public Map<String, Object> getSystemStats() {
        List<RiskScoreHistory> allScores = historyRepo.findAll();

        long totalUsers = userRepo.count();
        long highRiskUsers = allScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.HIGH)
                .map(RiskScoreHistory::getUser)
                .distinct()
                .count();

        long mediumRiskUsers = allScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.MEDIUM)
                .map(RiskScoreHistory::getUser)
                .distinct()
                .count();

        long lowRiskUsers = allScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.LOW)
                .map(RiskScoreHistory::getUser)
                .distinct()
                .count();

        double avgScore = allScores.stream()
                .mapToDouble(RiskScoreHistory::getScore)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("highRiskUsers", highRiskUsers);
        stats.put("mediumRiskUsers", mediumRiskUsers);
        stats.put("lowRiskUsers", lowRiskUsers);
        stats.put("averageTrustScore", avgScore);
        stats.put("totalScoreCalculations", allScores.size());

        return stats;
    }

    @GetMapping("/risk-distribution")
    @Operation(summary = "Get Risk Distribution", description = "Get distribution of users by risk level")
    public Map<String, Object> getRiskDistribution() {
        List<RiskScoreHistory> latestScores = historyRepo.findAll();

        Map<RiskLevel, Long> distribution = new HashMap<>();
        distribution.put(RiskLevel.HIGH, latestScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.HIGH).count());
        distribution.put(RiskLevel.MEDIUM, latestScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.MEDIUM).count());
        distribution.put(RiskLevel.LOW, latestScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.LOW).count());

        Map<String, Object> result = new HashMap<>();
        result.put("distribution", distribution);
        result.put("total", latestScores.size());

        return result;
    }

    @GetMapping("/score-trends")
    @Operation(summary = "Get Score Trends", description = "Get trust score trends over time")
    public Map<String, Object> getScoreTrends() {
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant last7d = Instant.now().minus(7, ChronoUnit.DAYS);

        List<RiskScoreHistory> last24hScores = historyRepo.findAll().stream()
                .filter(s -> s.getCalculatedAt().isAfter(last24h))
                .toList();

        List<RiskScoreHistory> last7dScores = historyRepo.findAll().stream()
                .filter(s -> s.getCalculatedAt().isAfter(last7d))
                .toList();

        Map<String, Object> trends = new HashMap<>();
        trends.put("last24Hours", Map.of(
                "count", last24hScores.size(),
                "avgScore", last24hScores.stream()
                        .mapToDouble(RiskScoreHistory::getScore).average().orElse(0.0)
        ));
        trends.put("last7Days", Map.of(
                "count", last7dScores.size(),
                "avgScore", last7dScores.stream()
                        .mapToDouble(RiskScoreHistory::getScore).average().orElse(0.0)
        ));

        return trends;
    }

    @GetMapping("/false-positive-rate")
    @Operation(summary = "Get False Positive Rate",
               description = "Calculate false positive rate (low-risk users marked as high-risk)")
    public Map<String, Object> getFalsePositiveRate() {
        // This is a simplified calculation
        // In production, you'd compare predicted vs actual outcomes
        List<RiskScoreHistory> allScores = historyRepo.findAll();

        long totalHighRisk = allScores.stream()
                .filter(s -> s.getLevel() == RiskLevel.HIGH)
                .count();

        // Simulate false positives (in reality, this would be tracked from actual incidents)
        // For demo purposes, assume ~5% false positive rate
        long estimatedFalsePositives = totalHighRisk / 20;

        double falsePositiveRate = totalHighRisk > 0 ?
                (double) estimatedFalsePositives / totalHighRisk : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("falsePositiveRate", falsePositiveRate);
        result.put("totalHighRiskPredictions", totalHighRisk);
        result.put("estimatedFalsePositives", estimatedFalsePositives);
        result.put("note", "This is an estimated metric. In production, track actual incidents.");

        return result;
    }

    @GetMapping("/performance")
    @Operation(summary = "Get Performance Metrics", description = "Get system performance metrics")
    public Map<String, Object> getPerformanceMetrics() {
        List<RiskScoreHistory> allScores = historyRepo.findAll();

        // Calculate performance metrics
        double avgEvaluationTime = 150.0; // ms (simulated)
        long totalEvaluations = allScores.size();

        Map<String, Object> performance = new HashMap<>();
        performance.put("totalEvaluations", totalEvaluations);
        performance.put("avgEvaluationTimeMs", avgEvaluationTime);
        performance.put("evaluationsPerHour", totalEvaluations / 24.0); // Approximate
        performance.put("systemUptime", "healthy");

        return performance;
    }
}
