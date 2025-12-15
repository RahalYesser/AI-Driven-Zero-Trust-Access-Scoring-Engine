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

    @GetMapping("/dashboard")
    @Operation(summary = "Get Dashboard Statistics", 
               description = "Get comprehensive dashboard statistics with risk distribution and explanations")
    public Map<String, Object> getDashboardStats() {
        List<RiskScoreHistory> allScores = historyRepo.findAll();
        long totalUsers = userRepo.count();
        
        // Calculate latest score per user
        Map<String, RiskScoreHistory> latestScores = new HashMap<>();
        allScores.forEach(score -> {
            String userId = score.getUser().getId().toString();
            if (!latestScores.containsKey(userId) || 
                score.getCalculatedAt().isAfter(latestScores.get(userId).getCalculatedAt())) {
                latestScores.put(userId, score);
            }
        });
        
        long highRiskUsers = latestScores.values().stream()
                .filter(s -> s.getLevel() == RiskLevel.HIGH).count();
        long mediumRiskUsers = latestScores.values().stream()
                .filter(s -> s.getLevel() == RiskLevel.MEDIUM).count();
        long lowRiskUsers = latestScores.values().stream()
                .filter(s -> s.getLevel() == RiskLevel.LOW).count();

        double avgScore = latestScores.values().stream()
                .mapToDouble(RiskScoreHistory::getScore).average().orElse(0.0);

        Map<String, Object> dashboard = new HashMap<>();
        
        // Main statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("highRiskUsers", highRiskUsers);
        stats.put("mediumRiskUsers", mediumRiskUsers);
        stats.put("lowRiskUsers", lowRiskUsers);
        stats.put("averageTrustScore", Math.round(avgScore * 10.0) / 10.0);
        stats.put("totalScoreCalculations", allScores.size());
        dashboard.put("stats", stats);
        
        // Risk distribution with percentages
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("HIGH", highRiskUsers);
        distribution.put("MEDIUM", mediumRiskUsers);
        distribution.put("LOW", lowRiskUsers);
        distribution.put("highPercentage", totalUsers > 0 ? Math.round((highRiskUsers * 100.0 / totalUsers) * 10) / 10.0 : 0);
        distribution.put("mediumPercentage", totalUsers > 0 ? Math.round((mediumRiskUsers * 100.0 / totalUsers) * 10) / 10.0 : 0);
        distribution.put("lowPercentage", totalUsers > 0 ? Math.round((lowRiskUsers * 100.0 / totalUsers) * 10) / 10.0 : 0);
        dashboard.put("distribution", distribution);
        
        // Explanations for each metric
        Map<String, String> explanations = new HashMap<>();
        explanations.put("totalUsers", "Total number of users registered in the system");
        explanations.put("highRiskUsers", "Users with trust score < 40 - May face access restrictions or account locks");
        explanations.put("mediumRiskUsers", "Users with trust score 40-75 - May require MFA for enhanced security");
        explanations.put("lowRiskUsers", "Users with trust score > 75 - Trusted users with full access");
        explanations.put("averageTrustScore", "Average trust score across all users (0-100 scale)");
        explanations.put("totalScoreCalculations", "Total number of trust score evaluations performed");
        explanations.put("riskLevels", "LOW (>75): Full Access | MEDIUM (40-75): MFA Required | HIGH (<40): Access Blocked");
        dashboard.put("explanations", explanations);
        
        return dashboard;
    }

    // Removed redundant endpoints - use /dashboard for comprehensive stats
}
