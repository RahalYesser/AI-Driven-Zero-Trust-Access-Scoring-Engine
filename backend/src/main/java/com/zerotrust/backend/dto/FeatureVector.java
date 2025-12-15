package com.zerotrust.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeatureVector {

    // -------- Behavioral --------
    double totalEvents;
    double failedLoginRate;
    double nightAccessRate;
    double loginFrequency24h;

    // -------- Device Posture --------
    double avgDeviceRisk;
    double unpatchedDeviceRatio;
    double antivirusDisabledRatio;

    // -------- Contextual --------
    double networkRiskScore;
    double locationChangeScore;
    double timeAnomalyScore;

    // -------- Account State --------
    double secondsSinceLastLogin;
}

