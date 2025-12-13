package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.RiskScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskScoreHistoryRepository extends JpaRepository<RiskScoreHistory, Long> {
    RiskScoreHistory findTopByUserEmailOrderByCalculatedAtDesc(String email);

}
