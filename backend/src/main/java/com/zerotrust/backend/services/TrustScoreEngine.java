package com.zerotrust.backend.services;

import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.entities.*;

import java.util.List;
import java.util.Map;

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
            List<Device> devices = deviceRepository.findByUser(user);
            List<AccessEvent> events = accessEventRepository.findByUser(user);

            Map<String,Object> features = featureService.extractFeatures(user, events, devices);
            double score = scoringService.computeTrustScore(features);
            RiskLevel risk = scoringService.getRiskLevel(score);
            loggingService.logScore(user, score, risk);

            System.out.println("User: " + user.getEmail() + " | Score: " + score + " | Risk: " + risk);
        }
    }
}

