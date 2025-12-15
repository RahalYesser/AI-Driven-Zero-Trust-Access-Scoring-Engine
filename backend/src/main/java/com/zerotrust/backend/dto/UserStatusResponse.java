package com.zerotrust.backend.dto;

import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusResponse {
    
    private UUID userId;
    private String email;
    private UserRole role;
    private double trustScore;
    private RiskLevel riskLevel;
    private boolean mfaEnabled;
    private boolean accountLocked;
    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private String message;
}
