package com.zerotrust.backend.services.features;

import com.zerotrust.backend.dto.FeatureVector;
import com.zerotrust.backend.entities.AccessEvent;
import com.zerotrust.backend.entities.Device;
import com.zerotrust.backend.entities.User;
import com.zerotrust.backend.enums.NetworkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class FeatureExtractionService {

    private static final double DEFAULT_NETWORK_RISK_SCORE = 30.0;

    public FeatureVector extract(
            User user,
            List<AccessEvent> events,
            List<Device> devices
    ) {

        long total = events.size();
        long failed = events.stream().filter(e -> !e.isSuccess()).count();

        double failedRate = total == 0 ? 0 : (double) failed / total;

        long nightEvents = events.stream()
                .filter(e -> e.getHourOfDay() < 6 || e.getHourOfDay() > 22)
                .count();

        double nightRate = total == 0 ? 0 : (double) nightEvents / total;

        long last24h = events.stream()
                .filter(e -> e.getTimestamp().isAfter(Instant.now().minusSeconds(86400)))
                .count();

        double avgDeviceRisk = devices.stream()
                .mapToDouble(Device::getDeviceRiskScore)
                .average().orElse(50);

        double unpatchedRatio = devices.isEmpty() ? 0 :
                devices.stream().filter(d -> !d.isPatched()).count() / (double) devices.size();

        double avDisabledRatio = devices.isEmpty() ? 0 :
                devices.stream().filter(d -> !d.isAntivirusEnabled()).count() / (double) devices.size();

        double networkRisk = events.stream()
                .mapToDouble(e -> networkRiskScoreFor(e.getNetworkType()))
                .average()
                .orElse(DEFAULT_NETWORK_RISK_SCORE);


        double locationChangeScore = computeLocationChange(events);
        double timeAnomalyScore = nightRate * 100;

        long secondsSinceLastLogin =
                user.getLastLoginAt() == null ? 0 :
                        Instant.now().getEpochSecond() - user.getLastLoginAt().getEpochSecond();

        return FeatureVector.builder()
                .totalEvents(total)
                .failedLoginRate(failedRate)
                .nightAccessRate(nightRate)
                .loginFrequency24h(last24h)
                .avgDeviceRisk(avgDeviceRisk)
                .unpatchedDeviceRatio(unpatchedRatio)
                .antivirusDisabledRatio(avDisabledRatio)
                .networkRiskScore(networkRisk)
                .locationChangeScore(locationChangeScore)
                .timeAnomalyScore(timeAnomalyScore)
                .secondsSinceLastLogin(secondsSinceLastLogin)
                .build();
    }

    private double computeLocationChange(List<AccessEvent> events) {
        return events.stream()
                .map(AccessEvent::getCountry)
                .distinct()
                .count() * 10.0;
    }
    private static double networkRiskScoreFor(NetworkType networkType) {
        if (networkType == null) {
            return DEFAULT_NETWORK_RISK_SCORE;
        }
        return switch (networkType) {
            case INTERNAL -> 10.0;
            case VPN -> 25.0;
            case EXTERNAL -> 45.0;
            case TOR -> 80.0;
        };
    }
}

