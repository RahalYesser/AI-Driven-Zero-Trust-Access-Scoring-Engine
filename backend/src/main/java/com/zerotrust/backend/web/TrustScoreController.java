package com.zerotrust.backend.web;

import com.zerotrust.backend.entities.RiskScoreHistory;
import com.zerotrust.backend.repositories.RiskScoreHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Trust Score History API
 * Note: Current trust scores are available via /api/auth/user-status
 */
@RestController
@RequestMapping("/api/risk-history")
@RequiredArgsConstructor
@Tag(name = "Risk History", description = "Historical risk score data for analysis")
public class TrustScoreController {

    private final RiskScoreHistoryRepository historyRepo;

    @GetMapping("/{userId}")
    @Operation(summary = "Get Risk Score History", 
               description = "Get historical risk scores for trend analysis (current score available in /api/auth/user-status)")
    public List<RiskScoreHistory> getHistory(
            @Parameter(description = "User ID") @PathVariable UUID userId){
        return historyRepo.findByUserIdOrderByCalculatedAtDesc(userId);
    }
}

