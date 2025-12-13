package com.zerotrust.backend.web;

import com.zerotrust.backend.entities.RiskScoreHistory;
import com.zerotrust.backend.repositories.RiskScoreHistoryRepository;
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
public class TrustScoreController {

    private final RiskScoreHistoryRepository historyRepo;

    @GetMapping("/trust-score/{userId}")
    public Map<String, Object> getCurrentScore(@PathVariable UUID userId){
        RiskScoreHistory latest = historyRepo.findTopByUserIdOrderByCalculatedAtDesc(userId);
        return Map.of(
                "score", latest.getScore(),
                "riskLevel", latest.getLevel(),
                "calculatedAt", latest.getCalculatedAt()
        );
    }

    @GetMapping("/risk-history/{userId}")
    public List<RiskScoreHistory> getHistory(@PathVariable UUID userId){
        return historyRepo.findByUserIdOrderByCalculatedAtDesc(userId);
    }
}

