package com.zerotrust.backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerotrust.backend.enums.RiskLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @NotNull
    Instant calculatedAt;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant updateAt;

    @Min(0)
    @Max(1)
    Double confidence;
    String modelName; // random_forest_v1
    String modelVersion;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
