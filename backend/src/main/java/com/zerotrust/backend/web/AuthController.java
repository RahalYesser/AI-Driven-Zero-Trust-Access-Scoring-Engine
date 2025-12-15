package com.zerotrust.backend.web;

import com.zerotrust.backend.dto.LoginRequest;
import com.zerotrust.backend.dto.LoginResponse;
import com.zerotrust.backend.dto.UserStatusResponse;
import com.zerotrust.backend.entities.User;
import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.UserRepository;
import com.zerotrust.backend.security.JwtService;
import com.zerotrust.backend.services.TrustScoreEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and login APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TrustScoreEngine trustScoreEngine;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and evaluate trust score for access decision")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Find user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.builder()
                                .decision("BLOCKED")
                                .message("Invalid credentials")
                                .build());
            }

            // Check if account is locked
            if (user.isAccountLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(LoginResponse.builder()
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .riskLevel(user.getCurrentRiskLevel())
                                .trustScore(user.getTrustScore())
                                .decision("BLOCKED")
                                .accountLocked(true)
                                .message("Account is locked. Please contact administrator.")
                                .build());
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Authentication successful - reset failed login attempts
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(Instant.now());

            // Compute real-time trust score
            try {
                trustScoreEngine.computeTrustScoreForUser(user);
            } catch (Exception e) {
                System.err.println("Error computing trust score: " + e.getMessage());
            }

            // Get updated user with new trust score
            user = userRepository.findByEmail(request.getEmail()).orElseThrow();

            double trustScore = user.getTrustScore() != null ? user.getTrustScore() : 50.0;
            RiskLevel riskLevel = user.getCurrentRiskLevel() != null ? user.getCurrentRiskLevel() : RiskLevel.MEDIUM;

            // Make access decision based on risk level
            String decision;
            String message;
            boolean mfaRequired = false;

            if (riskLevel == RiskLevel.HIGH) {
                // HIGH RISK: Block access and lock account
                decision = "BLOCKED";
                message = "Access denied. Your trust score is too low. Account has been locked.";
                user.setAccountLocked(true);
                userRepository.save(user);
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(LoginResponse.builder()
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .riskLevel(riskLevel)
                                .trustScore(trustScore)
                                .decision(decision)
                                .accountLocked(true)
                                .message(message)
                                .build());
                
            } else if (riskLevel == RiskLevel.MEDIUM) {
                // MEDIUM RISK: Require MFA
                decision = "REQUIRE_MFA";
                message = "Multi-Factor Authentication required due to medium risk level.";
                mfaRequired = true;
                user.setMfaEnabled(true);
                
            } else {
                // LOW RISK: Allow access
                decision = "ALLOW";
                message = "Login successful. Welcome!";
            }

            // Save user updates
            userRepository.save(user);

            // Generate JWT token
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(LoginResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .riskLevel(riskLevel)
                    .trustScore(trustScore)
                    .decision(decision)
                    .message(message)
                    .mfaRequired(mfaRequired)
                    .accountLocked(false)
                    .build());

        } catch (Exception e) {
            // Authentication failed - increment failed login attempts
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                
                // Lock account after 5 failed attempts
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setAccountLocked(true);
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(LoginResponse.builder()
                                    .decision("BLOCKED")
                                    .accountLocked(true)
                                    .message("Account locked due to multiple failed login attempts.")
                                    .build());
                }
                
                userRepository.save(user);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .decision("BLOCKED")
                            .message("Invalid credentials")
                            .build());
        }
    }

    @GetMapping("/user-status")
    @Transactional(readOnly = true)
    @Operation(summary = "Get User Status", description = "Get current user's trust score and account status")
    public ResponseEntity<?> getUserStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated", "message", "Please login again"));
        }

        String email = authentication.getName();
        
        if (email == null || email.isEmpty() || "anonymousUser".equals(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid authentication", "message", "Please login again"));
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return ResponseEntity.ok(UserStatusResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .trustScore(user.getTrustScore() != null ? user.getTrustScore() : 50.0)
                .riskLevel(user.getCurrentRiskLevel() != null ? user.getCurrentRiskLevel() : RiskLevel.MEDIUM)
                .mfaEnabled(user.isMfaEnabled())
                .accountLocked(user.isAccountLocked())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lastLoginAt(user.getLastLoginAt())
                .message("User status retrieved successfully")
                .build());
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logout current user")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }
}
