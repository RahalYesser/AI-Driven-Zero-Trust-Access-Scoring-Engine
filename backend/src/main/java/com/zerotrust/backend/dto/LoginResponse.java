package com.zerotrust.backend.dto;

import com.zerotrust.backend.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    
    private String token;
    private String email;
    private String role;
    private RiskLevel riskLevel;
    private double trustScore;
    private String decision; // "ALLOW", "REQUIRE_MFA", "BLOCKED"
    private String message;
    private boolean mfaRequired;
    private boolean accountLocked;
}
