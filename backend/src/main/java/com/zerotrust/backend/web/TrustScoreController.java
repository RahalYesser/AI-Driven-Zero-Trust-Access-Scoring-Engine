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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Trust Score", description = "User Trust Score & Risk History APIs")
public class TrustScoreController {

    private final RiskScoreHistoryRepository historyRepo;

    @GetMapping("/trust-score/{userId}")
    @Operation(summary = "Get Current Trust Score", description = "Get the latest trust score for a user")
    public Map<String, Object> getCurrentScore(
            @Parameter(description = "User ID") @PathVariable UUID userId){
        RiskScoreHistory latest = historyRepo.findTopByUserIdOrderByCalculatedAtDesc(userId);
        return Map.of(
                "score", latest.getScore(),
                "riskLevel", latest.getLevel(),
                "calculatedAt", latest.getCalculatedAt()
        );
    }

    @GetMapping("/risk-history/{userId}")
    @Operation(summary = "Get Risk History", description = "Get the risk score history for a user")
    public List<RiskScoreHistory> getHistory(
            @Parameter(description = "User ID") @PathVariable UUID userId){
        return historyRepo.findByUserIdOrderByCalculatedAtDesc(userId);
    }
}

