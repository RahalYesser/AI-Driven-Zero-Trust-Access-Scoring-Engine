package com.zerotrust.backend.services;

import com.zerotrust.backend.dto.FeatureVector;
import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.*;
import com.zerotrust.backend.services.features.FeatureExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.entities.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrustScoreEngine {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AccessEventRepository accessEventRepository;

    private final FeatureExtractionService featureService;
    private final TrustScoringService scoringService;
    private final RiskScoreLoggingService loggingService;

    @Scheduled(fixedRate = 300_000) // every 5 min
    public void computeAllTrustScores() throws Exception {
        List<User> users = userRepository.findAll();
        for(User user : users) {
            computeTrustScoreForUser(user);
        }
    }

    /**
     * Compute trust score for a specific user (used during login)
     */
    public void computeTrustScoreForUser(User user) throws Exception {
        List<Device> devices = deviceRepository.findByUser(user);
        List<AccessEvent> events = accessEventRepository.findByUser(user);

        FeatureVector features = featureService.extract(user, events, devices);
        double score = scoringService.compute(features);
        RiskLevel risk = scoringService.risk(score);
        
        // Update user with new score and risk level
        user.setTrustScore(score);
        user.setCurrentRiskLevel(risk);
        userRepository.save(user);
        
        // Log the score
        loggingService.logScore(user, score, risk);

        System.out.println("User: " + user.getEmail() + " | Score: " + score + " | Risk: " + risk);
    }
}

