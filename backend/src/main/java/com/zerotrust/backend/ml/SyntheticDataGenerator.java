package com.zerotrust.backend.ml;

import com.zerotrust.backend.dto.FeatureVector;
import weka.core.Instances;

import java.util.Random;

/**
 * Generates synthetic training data for Zero-Trust ML model
 * Creates realistic feature vectors with labeled trust scores
 */
public class SyntheticDataGenerator {

    private final Random random;

    public SyntheticDataGenerator(long seed) {
        this.random = new Random(seed);
    }

    public SyntheticDataGenerator() {
        this(42L);
    }

    /**
     * Generate synthetic training dataset with balanced risk distribution
     * @param numSamples number of training samples to generate
     * @return Weka Instances with labeled data
     */
    public Instances generateTrainingData(int numSamples) {
        Instances dataset = WekaDatasetBuilder.buildDataset(true);

        // Ensure balanced distribution: 1/3 LOW, 1/3 MEDIUM, 1/3 HIGH risk
        int samplesPerRisk = numSamples / 3;
        
        for (int i = 0; i < numSamples; i++) {
            // Force balanced distribution
            int riskProfile;
            if (i < samplesPerRisk) {
                riskProfile = 0; // LOW
            } else if (i < samplesPerRisk * 2) {
                riskProfile = 1; // MEDIUM
            } else {
                riskProfile = 2; // HIGH
            }
            
            FeatureVector features = generateRandomFeatures(riskProfile);
            double trustScore = computeLabeledScore(features);

            double[] values = new double[dataset.numAttributes()];
            values[0] = features.getFailedLoginRate();
            values[1] = features.getNightAccessRate();
            values[2] = features.getLoginFrequency24h();
            values[3] = features.getAvgDeviceRisk();
            values[4] = features.getUnpatchedDeviceRatio();
            values[5] = features.getAntivirusDisabledRatio();
            values[6] = features.getNetworkRiskScore();
            values[7] = features.getLocationChangeScore();
            values[8] = features.getTimeAnomalyScore();
            values[9] = features.getSecondsSinceLastLogin();
            values[10] = trustScore; // label

            dataset.add(new weka.core.DenseInstance(1.0, values));
        }

        return dataset;
    }

    /**
     * Generate realistic feature vector with controlled randomness
     * @param riskProfile 0=LOW, 1=MEDIUM, 2=HIGH risk
     */
    private FeatureVector generateRandomFeatures(int riskProfile) {
        if (riskProfile == 0) {
            // LOW RISK profile
            return FeatureVector.builder()
                    .totalEvents(random.nextInt(50) + 50) // 50-100
                    .failedLoginRate(random.nextDouble() * 0.05) // 0-5%
                    .nightAccessRate(random.nextDouble() * 0.1) // 0-10%
                    .loginFrequency24h(random.nextInt(5) + 3) // 3-8
                    .avgDeviceRisk(random.nextDouble() * 20 + 10) // 10-30
                    .unpatchedDeviceRatio(random.nextDouble() * 0.1) // 0-10%
                    .antivirusDisabledRatio(random.nextDouble() * 0.05) // 0-5%
                    .networkRiskScore(random.nextDouble() * 15 + 10) // 10-25
                    .locationChangeScore(random.nextDouble() * 10) // 0-10
                    .timeAnomalyScore(random.nextDouble() * 15) // 0-15
                    .secondsSinceLastLogin(random.nextInt(3600) + 3600) // 1-2 hours
                    .build();

        } else if (riskProfile == 1) {
            // MEDIUM RISK profile
            return FeatureVector.builder()
                    .totalEvents(random.nextInt(50) + 20) // 20-70
                    .failedLoginRate(random.nextDouble() * 0.15 + 0.05) // 5-20%
                    .nightAccessRate(random.nextDouble() * 0.25 + 0.1) // 10-35%
                    .loginFrequency24h(random.nextInt(10) + 5) // 5-15
                    .avgDeviceRisk(random.nextDouble() * 30 + 30) // 30-60
                    .unpatchedDeviceRatio(random.nextDouble() * 0.3 + 0.1) // 10-40%
                    .antivirusDisabledRatio(random.nextDouble() * 0.2 + 0.05) // 5-25%
                    .networkRiskScore(random.nextDouble() * 25 + 25) // 25-50
                    .locationChangeScore(random.nextDouble() * 30 + 10) // 10-40
                    .timeAnomalyScore(random.nextDouble() * 30 + 15) // 15-45
                    .secondsSinceLastLogin(random.nextInt(7200) + 7200) // 2-4 hours
                    .build();

        } else {
            // HIGH RISK profile
            return FeatureVector.builder()
                    .totalEvents(random.nextInt(30) + 10) // 10-40
                    .failedLoginRate(random.nextDouble() * 0.4 + 0.2) // 20-60%
                    .nightAccessRate(random.nextDouble() * 0.4 + 0.35) // 35-75%
                    .loginFrequency24h(random.nextInt(20) + 15) // 15-35
                    .avgDeviceRisk(random.nextDouble() * 30 + 60) // 60-90
                    .unpatchedDeviceRatio(random.nextDouble() * 0.5 + 0.4) // 40-90%
                    .antivirusDisabledRatio(random.nextDouble() * 0.6 + 0.25) // 25-85%
                    .networkRiskScore(random.nextDouble() * 30 + 50) // 50-80
                    .locationChangeScore(random.nextDouble() * 50 + 40) // 40-90
                    .timeAnomalyScore(random.nextDouble() * 40 + 45) // 45-85
                    .secondsSinceLastLogin(random.nextInt(86400) + 14400) // 4-28 hours
                    .build();
        }
    }

    /**
     * Compute labeled trust score using rule-based logic
     * This creates ground truth for supervised learning
     */
    private double computeLabeledScore(FeatureVector f) {
        double score = 100.0;

        // Penalize failed logins (high weight)
        score -= f.getFailedLoginRate() * 80;

        // Penalize night access
        score -= f.getNightAccessRate() * 30;

        // Penalize high login frequency (potential brute force)
        if (f.getLoginFrequency24h() > 20) {
            score -= (f.getLoginFrequency24h() - 20) * 2;
        }

        // Device posture penalties
        score -= (f.getAvgDeviceRisk() / 100.0) * 25;
        score -= f.getUnpatchedDeviceRatio() * 30;
        score -= f.getAntivirusDisabledRatio() * 35;

        // Network and location penalties
        score -= (f.getNetworkRiskScore() / 100.0) * 20;
        score -= f.getLocationChangeScore() * 0.8;
        score -= f.getTimeAnomalyScore() * 0.5;

        // Stale login penalty
        long hoursSinceLogin = (long)f.getSecondsSinceLastLogin() / 3600;
        if (hoursSinceLogin > 24) {
            score -= (hoursSinceLogin - 24) * 0.5;
        }

        // Add small random noise
        score += (random.nextDouble() - 0.5) * 5;

        // Clamp to [0, 100]
        return Math.max(0, Math.min(100, score));
    }
}
