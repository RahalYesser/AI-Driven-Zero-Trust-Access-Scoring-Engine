package com.zerotrust.backend.bootstrap;

import com.zerotrust.backend.entities.*;
import com.zerotrust.backend.enums.*;
import com.zerotrust.backend.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AccessEventRepository accessEventRepository;
    private final RiskScoreHistoryRepository riskScoreHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        Random random = new Random();

        List<User> users = new ArrayList<>();
        
        // Generate 50 users with balanced risk profiles
        // 15 LOW risk, 20 MEDIUM risk, 15 HIGH risk
        for (int i = 1; i <= 50; i++) {
            RiskLevel riskLevel;
            double trustScore;
            int failedAttempts;
            boolean mfaEnabled;
            long lastLoginSeconds;
            
            if (i <= 15) {
                // LOW RISK users (30%)
                riskLevel = RiskLevel.LOW;
                trustScore = 75.0 + random.nextDouble() * 25; // 75-100
                failedAttempts = 0;
                mfaEnabled = true;
                lastLoginSeconds = random.nextInt(3600 * 12); // within 12 hours
            } else if (i <= 35) {
                // MEDIUM RISK users (40%)
                riskLevel = RiskLevel.MEDIUM;
                trustScore = 40.0 + random.nextDouble() * 35; // 40-75
                failedAttempts = random.nextInt(3) + 1; // 1-3
                mfaEnabled = random.nextBoolean();
                lastLoginSeconds = random.nextInt(3600 * 48); // within 2 days
            } else {
                // HIGH RISK users (30%)
                riskLevel = RiskLevel.HIGH;
                trustScore = 10.0 + random.nextDouble() * 30; // 10-40
                failedAttempts = random.nextInt(5) + 3; // 3-7
                mfaEnabled = false;
                lastLoginSeconds = random.nextInt(3600 * 168); // within 1 week
            }
            
            User user = User.builder()
                    .email("user" + i + "@company.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(i % 5 == 0 ? UserRole.ADMIN : UserRole.MANAGER)
                    .trustScore(trustScore)
                    .currentRiskLevel(riskLevel)
                    .mfaEnabled(mfaEnabled)
                    .accountLocked(false)
                    .failedLoginAttempts(failedAttempts)
                    .lastLoginAt(Instant.now().minusSeconds(lastLoginSeconds))
                    .build();
            users.add(user);
        }
        userRepository.saveAll(users);

        List<Device> devices = new ArrayList<>();
        for (User user : users) {
            int deviceCount = 2 + random.nextInt(3); // 2-4 devices
            RiskLevel userRisk = user.getCurrentRiskLevel();
            
            for (int j = 1; j <= deviceCount; j++) {
                boolean patched;
                boolean antivirusEnabled;
                DeviceTrustLevel trustLevel;
                double deviceRiskScore;
                
                // Align device security with user risk profile
                if (userRisk == RiskLevel.LOW) {
                    patched = random.nextDouble() < 0.9; // 90% patched
                    antivirusEnabled = random.nextDouble() < 0.95; // 95% antivirus
                    trustLevel = DeviceTrustLevel.TRUSTED;
                    deviceRiskScore = random.nextDouble() * 30; // 0-30
                } else if (userRisk == RiskLevel.MEDIUM) {
                    patched = random.nextDouble() < 0.5; // 50% patched
                    antivirusEnabled = random.nextDouble() < 0.6; // 60% antivirus
                    trustLevel = random.nextBoolean() ? DeviceTrustLevel.TRUSTED : DeviceTrustLevel.UNTRUSTED;
                    deviceRiskScore = 30 + random.nextDouble() * 40; // 30-70
                } else {
                    patched = random.nextDouble() < 0.2; // 20% patched
                    antivirusEnabled = random.nextDouble() < 0.3; // 30% antivirus
                    trustLevel = DeviceTrustLevel.UNTRUSTED;
                    deviceRiskScore = 60 + random.nextDouble() * 40; // 60-100
                }
                
                Device device = Device.builder()
                        .deviceName(user.getEmail().split("@")[0] + "-device" + j)
                        .os(randomOS())
                        .osVersion(randomVersion())
                        .patched(patched)
                        .antivirusEnabled(antivirusEnabled)
                        .trustLevel(trustLevel)
                        .deviceRiskScore(deviceRiskScore)
                        .lastSeenAt(Instant.now().minusSeconds(random.nextInt(3600 * 24 * 7)))
                        .user(user)
                        .build();
                devices.add(device);
            }
        }
        deviceRepository.saveAll(devices);

        List<AccessEvent> events = new ArrayList<>();
        for (Device device : devices) {
            User user = device.getUser();
            RiskLevel userRisk = user.getCurrentRiskLevel();
            int eventsCount = 15 + random.nextInt(25); // 15-40 events per device
            
            for (int k = 0; k < eventsCount; k++) {
                boolean success;
                int hourOfDay;
                boolean weekend;
                NetworkType networkType;
                double eventRiskScore;
                long eventAgeSeconds;
                
                // Align access patterns with user risk profile
                if (userRisk == RiskLevel.LOW) {
                    success = random.nextDouble() < 0.98; // 98% success
                    hourOfDay = 8 + random.nextInt(10); // mostly business hours 8-18
                    weekend = random.nextDouble() < 0.1; // 10% weekend access
                    networkType = NetworkType.INTERNAL;
                    eventRiskScore = random.nextDouble() * 25; // 0-25
                    eventAgeSeconds = random.nextInt(3600 * 24 * 7); // within 7 days
                } else if (userRisk == RiskLevel.MEDIUM) {
                    success = random.nextDouble() < 0.85; // 85% success
                    hourOfDay = random.nextInt(24); // any hour
                    weekend = random.nextDouble() < 0.3; // 30% weekend access
                    networkType = random.nextBoolean() ? NetworkType.INTERNAL : NetworkType.VPN;
                    eventRiskScore = 25 + random.nextDouble() * 40; // 25-65
                    eventAgeSeconds = random.nextInt(3600 * 24 * 14); // within 14 days
                } else {
                    success = random.nextDouble() < 0.7; // 70% success, more failures
                    hourOfDay = random.nextInt(24); // any hour, including night
                    weekend = random.nextDouble() < 0.5; // 50% weekend access
                    networkType = random.nextBoolean() ? NetworkType.VPN : NetworkType.EXTERNAL;
                    eventRiskScore = 60 + random.nextDouble() * 40; // 60-100
                    eventAgeSeconds = random.nextInt(3600 * 24 * 30); // within 30 days
                }
                
                AccessEvent event = AccessEvent.builder()
                        .user(user)
                        .device(device)
                        .timestamp(Instant.now().minusSeconds(eventAgeSeconds))
                        .ipAddress(randomIP())
                        .networkType(networkType)
                        .country(randomCountry())
                        .city(randomCity())
                        .hourOfDay(hourOfDay)
                        .weekend(weekend)
                        .resource(randomResource())
                        .sessionId(UUID.randomUUID().toString())
                        .userAgent(randomUserAgent())
                        .success(success)
                        .eventRiskScore(eventRiskScore)
                        .build();
                events.add(event);
            }
        }
        accessEventRepository.saveAll(events);

        // Create RiskScoreHistory for each user
        for (User user : users) {
            // Create 3-5 historical entries per user
            int historyCount = 3 + random.nextInt(3);
            for (int h = 0; h < historyCount; h++) {
                riskScoreHistoryRepository.save(
                        RiskScoreHistory.builder()
                                .user(user)
                                .score(user.getTrustScore() + (random.nextDouble() - 0.5) * 10)
                                .level(user.getCurrentRiskLevel())
                                .confidence(0.75 + random.nextDouble() * 0.25)
                                .modelName("random_forest")
                                .modelVersion("v1")
                                .calculatedAt(Instant.now().minusSeconds(random.nextInt(3600 * 24 * 7)))
                                .build()
                );
            }
        }

        System.out.println("✅ Loaded " + users.size() + " users, " + devices.size() + " devices, " + events.size() + " access events");
        System.out.println("   Risk distribution: LOW=15, MEDIUM=20, HIGH=15");
        
        // Print score distribution
        long lowCount = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.LOW).count();
        long medCount = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.MEDIUM).count();
        long highCount = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.HIGH).count();
        double avgLowScore = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.LOW).mapToDouble(User::getTrustScore).average().orElse(0);
        double avgMedScore = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.MEDIUM).mapToDouble(User::getTrustScore).average().orElse(0);
        double avgHighScore = users.stream().filter(u -> u.getCurrentRiskLevel() == RiskLevel.HIGH).mapToDouble(User::getTrustScore).average().orElse(0);
        System.out.println(String.format("   Avg Trust Scores: LOW=%.1f, MEDIUM=%.1f, HIGH=%.1f", avgLowScore, avgMedScore, avgHighScore));
    }

    // ---------------- Helper randomizers ----------------
    private String randomOS() {
        String[] os = {"Windows", "macOS", "Ubuntu", "Android", "iOS"};
        return os[new Random().nextInt(os.length)];
    }
    private String randomVersion() {
        return  "v" + (1 + new Random().nextInt(15)) + "." + new Random().nextInt(10);
    }
    private String randomIP() {
        Random r = new Random();
        return r.nextInt(256)+"."+r.nextInt(256)+"."+r.nextInt(256)+"."+r.nextInt(256);
    }
    private NetworkType randomNetworkType() {
        return new Random().nextBoolean() ? NetworkType.VPN : NetworkType.INTERNAL;
    }
    private String randomCountry() {
        String[] countries = {"USA","Tunisia","Germany","India","France","Unknown"};
        return countries[new Random().nextInt(countries.length)];
    }
    private String randomCity() {
        String[] cities = {"Sfax","Tunis","Berlin","Paris","New York","Unknown"};
        return cities[new Random().nextInt(cities.length)];
    }
    private String randomResource() {
        String[] res = {"/api/documents","/api/admin","/api/settings","/api/data"};
        return res[new Random().nextInt(res.length)];
    }
    private String randomUserAgent() {
        String[] agents = {"Chrome","Firefox","Edge","Safari","Unknown"};
        return agents[new Random().nextInt(agents.length)];
    }
}
