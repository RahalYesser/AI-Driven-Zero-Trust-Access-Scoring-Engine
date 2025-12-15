package com.zerotrust.backend.services;

import com.zerotrust.backend.dto.FeatureVector;
import com.zerotrust.backend.services.trust.TrustModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.enums.RiskLevel;

@Service
@RequiredArgsConstructor
public class TrustScoringService {

    private final TrustModel trustModel;

    public double compute(FeatureVector features) throws Exception {
        return trustModel.score(features);
    }

    public RiskLevel risk(double score) {
        if (score < 40) return RiskLevel.HIGH;
        if (score < 70) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}

