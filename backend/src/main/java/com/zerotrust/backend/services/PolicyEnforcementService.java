package com.zerotrust.backend.services;

import com.zerotrust.backend.enums.AccessDecision;
import com.zerotrust.backend.enums.RiskLevel;
import org.springframework.stereotype.Service;

@Service
public class PolicyEnforcementService {

    public AccessDecision enforce(RiskLevel risk) {
        return switch (risk) {
            case HIGH -> AccessDecision.DENY;
            case MEDIUM -> AccessDecision.WARN;
            case LOW -> AccessDecision.ALLOW;
        };
    }
}
