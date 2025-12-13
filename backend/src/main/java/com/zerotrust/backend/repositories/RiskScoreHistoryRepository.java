package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.RiskScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskScoreHistoryRepository extends JpaRepository<RiskScoreHistory, UUID> {
    RiskScoreHistory findTopByUserEmailOrderByCalculatedAtDesc(String email);
    List<RiskScoreHistory> findByUserIdOrderByCalculatedAtDesc(UUID userId);
    RiskScoreHistory findTopByUserIdOrderByCalculatedAtDesc(UUID userId);
}
