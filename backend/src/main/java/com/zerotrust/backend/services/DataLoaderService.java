package com.zerotrust.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.entities.*;
import com.zerotrust.backend.enums.UserRole;
import com.zerotrust.backend.repositories.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AccessEventRepository accessEventRepository;

    private final Random random = new Random();

    public void generateSampleData(int numUsers) {

        List<User> users = new ArrayList<>();

        // --- Generate Users ---
        for (int i = 1; i <= numUsers; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .password("Password@123") // hashed later in real app
                    .role(i % 2 == 0 ? UserRole.MANAGER : UserRole.ADMIN)
                    .trustScore(50.0 + random.nextDouble() * 50) // 50-100 initial
                    .accountLocked(false)
                    .failedLoginAttempts(0)
                    .lastLoginAt(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS))
                    .createdAt(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
                    .updateAt(Instant.now())
                    .mfaEnabled(random.nextBoolean())
                    .build();

            users.add(user);
        }
        userRepository.saveAll(users);

        // --- Generate Devices ---
        List<Device> devices = new ArrayList<>();
        for (User user : users) {
            int numDevices = 1 + random.nextInt(3); // 1-3 devices per user
            for (int d = 1; d <= numDevices; d++) {
                Device device = Device.builder()
                        .deviceName("Device-" + d + "-" + user.getEmail())
                        .os(randomOS())
                        .osVersion(randomVersion())
                        .patched(random.nextBoolean())
                        .antivirusEnabled(random.nextBoolean())
                        .deviceRiskScore(random.nextDouble() * 100)
                        .lastSeenAt(Instant.now().minus(random.nextInt(10), ChronoUnit.DAYS))
                        .user(user)
                        .build();
                devices.add(device);
            }
        }
        deviceRepository.saveAll(devices);

        // --- Generate Access Events ---
        List<AccessEvent> events = new ArrayList<>();
        for (User user : users) {
            List<Device> userDevices = deviceRepository.findByUser(user);
            int numEvents = 10 + random.nextInt(20); // 10-30 events per user
            for (int e = 0; e < numEvents; e++) {
                Device device = userDevices.get(random.nextInt(userDevices.size()));
                AccessEvent event = AccessEvent.builder()
                        .timestamp(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS))
                        .ipAddress(randomIp())
                        .geoLocation(randomLocation())
                        .resource(randomResource())
                        .success(random.nextDouble() > 0.1) // 90% success rate
                        .eventRiskScore(random.nextDouble() * 100)
                        .user(user)
                        .device(device)
                        .build();
                events.add(event);
            }
        }
        accessEventRepository.saveAll(events);

        System.out.println("✅ Sample data generated: Users=" + users.size()
                + ", Devices=" + devices.size()
                + ", AccessEvents=" + events.size());
    }

    // --- Helpers ---
    private String randomOS() {
        String[] osList = {"Windows", "Linux", "MacOS"};
        return osList[random.nextInt(osList.length)];
    }

    private String randomVersion() {
        int major = 1 + random.nextInt(10);
        int minor = random.nextInt(10);
        int patch = random.nextInt(10);
        return major + "." + minor + "." + patch;
    }

    private String randomIp() {
        return random.nextInt(256) + "." + random.nextInt(256) + "."
                + random.nextInt(256) + "." + random.nextInt(256);
    }

    private String randomLocation() {
        String[] locations = {"TN","US", "UK", "DE", "FR", "IN", "CN", "JP"};
        return locations[random.nextInt(locations.length)];
    }

    private String randomResource() {
        String[] resources = {"/api/data", "/api/admin", "/dashboard", "/login", "/settings"};
        return resources[random.nextInt(resources.length)];
    }
}

