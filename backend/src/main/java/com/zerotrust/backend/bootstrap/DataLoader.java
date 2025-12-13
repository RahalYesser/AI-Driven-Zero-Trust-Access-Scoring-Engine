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
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                    .email("user" + i + "@company.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(i % 3 == 0 ? UserRole.ADMIN : UserRole.MANAGER)
                    .trustScore(50.0 + random.nextDouble() * 50)
                    .currentRiskLevel(RiskLevel.LOW)
                    .mfaEnabled(i % 2 == 0)
                    .accountLocked(false)
                    .failedLoginAttempts(random.nextInt(5))
                    .lastLoginAt(Instant.now().minusSeconds(random.nextInt(3600 * 24 * 30)))
                    .build();
            users.add(user);
        }
        userRepository.saveAll(users);

        List<Device> devices = new ArrayList<>();
        for (User user : users) {
            int deviceCount = 2 + random.nextInt(4); // 2-5 devices
            for (int j = 1; j <= deviceCount; j++) {
                Device device = Device.builder()
                        .deviceName(user.getEmail().split("@")[0] + "-device" + j)
                        .os(randomOS())
                        .osVersion(randomVersion())
                        .patched(random.nextBoolean())
                        .antivirusEnabled(random.nextBoolean())
                        .trustLevel(random.nextBoolean() ? DeviceTrustLevel.TRUSTED : DeviceTrustLevel.UNTRUSTED)
                        .deviceRiskScore(random.nextDouble() * 100)
                        .lastSeenAt(Instant.now().minusSeconds(random.nextInt(3600 * 24 * 7)))
                        .user(user)
                        .build();
                devices.add(device);
            }
        }
        deviceRepository.saveAll(devices);

        List<AccessEvent> events = new ArrayList<>();
        for (Device device : devices) {
            int eventsCount = 5 + random.nextInt(10); // 5-15 events
            for (int k = 0; k < eventsCount; k++) {
                AccessEvent event = AccessEvent.builder()
                        .user(device.getUser())
                        .device(device)
                        .ipAddress(randomIP())
                        .networkType(randomNetworkType())
                        .country(randomCountry())
                        .city(randomCity())
                        .hourOfDay(random.nextInt(24))
                        .weekend(random.nextBoolean())
                        .resource(randomResource())
                        .sessionId(UUID.randomUUID().toString())
                        .userAgent(randomUserAgent())
                        .success(random.nextDouble() > 0.1)
                        .eventRiskScore(random.nextDouble() * 100)
                        .build();
                events.add(event);
            }
        }
        accessEventRepository.saveAll(events);

        // Optional: RiskScoreHistory
        for (User user : users) {
            riskScoreHistoryRepository.save(
                    RiskScoreHistory.builder()
                            .user(user)
                            .score(user.getTrustScore())
                            .level(user.getCurrentRiskLevel())
                            .confidence(0.8 + random.nextDouble() * 0.2)
                            .modelName("random_forest")
                            .modelVersion("v1")
                            .calculatedAt(Instant.now())
                            .build()
            );
        }

        System.out.println("✅ Loaded realistic Zero-Trust test data");
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
