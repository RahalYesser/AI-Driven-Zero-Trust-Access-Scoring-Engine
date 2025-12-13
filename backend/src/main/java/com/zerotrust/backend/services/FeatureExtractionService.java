package com.zerotrust.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.entities.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureExtractionService {

    /**
     * Extract ML-ready features for a user
     */
    public Map<String, Object> extractFeatures(User user, List<AccessEvent> events, List<Device> devices) {
        Map<String, Object> features = new HashMap<>();

        // --- Behavioral Features ---
        long totalEvents = events.size();
        long failedLogins = events.stream().filter(e -> !e.isSuccess()).count();
        features.put("totalEvents", totalEvents);
        features.put("failedLogins", failedLogins);
        features.put("failedLoginRate", totalEvents == 0 ? 0.0 : (double) failedLogins / totalEvents);

        long nightAccessEvents = events.stream()
                .filter(e -> {
                    LocalTime t = LocalTime.ofInstant(e.getTimestamp(), java.time.ZoneId.of("UTC"));
                    return t.isBefore(LocalTime.of(6,0)) || t.isAfter(LocalTime.of(22,0));
                }).count();
        features.put("nightAccessRate", totalEvents == 0 ? 0.0 : (double) nightAccessEvents / totalEvents);

        // --- Device Posture ---
        double avgDeviceRisk = devices.stream().mapToDouble(Device::getDeviceRiskScore).average().orElse(50.0);
        features.put("avgDeviceRisk", avgDeviceRisk);
        long unpatchedDevices = devices.stream().filter(d -> !d.isPatched()).count();
        features.put("unpatchedDevices", unpatchedDevices);

        // --- Contextual Features ---
       // long vpnAccessCount = events.stream().filter(AccessEvent::isVpn).count();
        // features.put("vpnAccessRate", totalEvents == 0 ? 0.0 : (double) vpnAccessCount / totalEvents);

        Instant lastLogin = user.getLastLoginAt() != null ? user.getLastLoginAt() : Instant.now();
        long secondsSinceLastLogin = Instant.now().getEpochSecond() - lastLogin.getEpochSecond();
        features.put("secondsSinceLastLogin", secondsSinceLastLogin);

        return features;
    }
}
