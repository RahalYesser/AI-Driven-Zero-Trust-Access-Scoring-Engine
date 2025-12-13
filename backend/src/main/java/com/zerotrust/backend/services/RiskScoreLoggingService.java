package com.zerotrust.backend.services;

import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.RiskScoreHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.entities.*;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RiskScoreLoggingService {

    private final RiskScoreHistoryRepository historyRepository;

    public void logScore(User user, double score, RiskLevel level) {
        RiskScoreHistory history = RiskScoreHistory.builder()
                .user(user)
                .score(score)
                .level(level)
                .calculatedAt(Instant.now())
                .build();
        historyRepository.save(history);
    }
}

