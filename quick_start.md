# Zero-Trust Backend – Quick Start & Project Goal

## 🎯 Project Goal
This project implements an **AI-Driven Zero-Trust Access Scoring Engine** using **Spring Boot 3.x** and **Spring Security 6.x**.

The objective is to dynamically evaluate the trustworthiness of **users, devices, and access requests** based on behavioral patterns, device posture, and contextual risk factors, then enforce access decisions in real time.

---

## 🧱 Final Target Architecture (Backend)

```
tn.sesame.zerotrust_backend
├── ZerotrustBackendApplication.java
├── config
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── SchedulerConfig.java
│   └── DockerConfig.java (optional)
│
├── security
│   ├── TrustScoreFilter.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConstants.java
│
├── entities
│   ├── User.java
│   ├── Device.java
│   ├── AccessEvent.java
│   ├── RiskScoreHistory.java
│   └── ApplicationResource.java
│
├── enums
│   ├── UserRole.java
│   ├── DeviceTrustLevel.java
│   ├── NetworkType.java
│   ├── RiskLevel.java
│   └── AccessDecision.java
│
├── repositories
│   ├── UserRepository.java
│   ├── DeviceRepository.java
│   ├── AccessEventRepository.java
│   └── RiskScoreHistoryRepository.java
│
├── services
│   ├── FeatureExtractionService.java
│   ├── MLModelService.java
│   ├── TrustScoringService.java
│   ├── PolicyEnforcementService.java
│   ├── AccessEventService.java
│   └── RiskScoreHistoryService.java
│
├── controllers
│   ├── AuthController.java
│   ├── AccessEventController.java
│   ├── TrustScoreController.java
│   └── AdminController.java
│
├── dto
│   ├── AccessEventDTO.java
│   ├── TrustScoreDTO.java
│   └── DevicePostureDTO.java
│
├── scheduler
│   └── TrustScoreScheduler.java
│
└── loader
    └── DataLoader.java
```

---

## ✅ What Is Currently Implemented

- Core **entities & enums** (User, Device, AccessEvent, RiskScoreHistory)
- PostgreSQL persistence with Spring Data JPA
- Docker & Docker Compose (backend + database)
- Development **DataLoader** with realistic fake data:
  - ~10 users
  - ~30 devices
  - ~100+ access events
- Basic Zero-Trust enforcement via `TrustScoreFilter`
- Password encoding with Spring Security

---

## 🚀 How to Run the Project

### 1️⃣ Start the application
```bash
docker compose up --build
```

- `--build` is required when:
  - Java code changes
  - Dependencies change
  - Dockerfile is modified

- Use `docker compose up` only when no code/image changes occurred.

---

## 🗄️ Database Access

- Database: **PostgreSQL** (Docker container)
- Data is auto-loaded on startup when profile = `dev`
- Tables created automatically via JPA (`ddl-auto=update`)

You can inspect data using:
- pgAdmin
- DBeaver
- psql inside the container

---

## 🔐 Zero-Trust Flow (Current)

1. User authenticates
2. Access events are recorded
3. Trust score is computed (initial static version)
4. Latest risk score is checked via `TrustScoreFilter`
5. High-risk users are denied access

---

## 🧠 Next Planned Steps

- Feature extraction for ML-ready vectors
- ML model integration (Random Forest / DL4J)
- Confidence-based trust scoring
- Scheduled re-evaluation of trust
- React dashboard for visualization
- Actuator metrics for model performance

---

📌 **This document reflects the final backend goal and current progress of the Zero-Trust AI project.**