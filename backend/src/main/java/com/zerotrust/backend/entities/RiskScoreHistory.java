package com.zerotrust.backend.entities;

import com.zerotrust.backend.enums.RiskLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "risk_score_history")
public class RiskScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Min(0)
    @Max(100)
    Double score;

    @Enumerated(EnumType.STRING)
    RiskLevel level;

    Instant calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
