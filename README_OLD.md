# 🛡️ AI-Driven Zero-Trust Access Scoring Engine

> Advanced ML-powered Zero-Trust security system for real-time access control and risk assessment

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.0-blue)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Weka](https://img.shields.io/badge/Weka-3.8.0-orange)](https://www.cs.waikato.ac.nz/ml/weka/)

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [ML Model](#ml-model)
- [Dashboard](#dashboard)
- [Project Structure](#project-structure)

## 🎯 Overview

This project implements a comprehensive **AI-Driven Zero-Trust Access Scoring Engine** that:
- Continuously evaluates user trustworthiness using behavioral analytics
- Analyzes device posture and network context
- Uses Random Forest ML model for real-time risk scoring
- Enforces dynamic access policies based on trust levels
- Provides real-time dashboard for monitoring and visualization

## ✨ Features

### Core Functionality
- ✅ **ML-Based Trust Scoring**: Random Forest model trained on 10 behavioral/contextual features
- ✅ **Feature Engineering**: User behavior, device posture, network context analysis
- ✅ **Synthetic Data Generation**: Automated training data generation for model development
- ✅ **Model Persistence**: Save/load trained models with versioning
- ✅ **Real-time Evaluation**: Continuous trust score computation (every 5 minutes)
- ✅ **Policy Enforcement**: Automatic access control based on risk levels

### API & Documentation
- ✅ **REST API**: Complete API for trust scores, metrics, and admin operations
- ✅ **Swagger/OpenAPI**: Interactive API documentation at `/swagger-ui.html`
- ✅ **Spring Boot Actuator**: System health and metrics endpoints
- ✅ **CORS Support**: Configured for frontend integration

### Metrics & Monitoring
- ✅ **Performance Metrics**: MAE, RMSE, Correlation Coefficient
- ✅ **Confusion Matrix**: True/False Positive/Negative rates
- ✅ **System Statistics**: User counts, risk distribution, trends
- ✅ **Real-time Dashboard**: Live visualization of trust scores and metrics

### Dashboard
- ✅ **Trust Score Visualization**: Real-time score display with risk indicators
- ✅ **Risk Distribution Charts**: Pie charts showing user risk levels
- ✅ **Model Management UI**: Train and evaluate models from the dashboard
- ✅ **Metrics Display**: False positive/negative rates, accuracy, performance

## 🏗️ Architecture

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│  React Frontend │◄────►│  Spring Boot API │◄────►│   PostgreSQL    │
│   (Dashboard)   │      │   (REST + ML)    │      │   (Data Store)  │
└─────────────────┘      └──────────────────┘      └─────────────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   Weka ML Model  │
                         │  (Random Forest) │
                         └──────────────────┘
```

### Zero-Trust Pipeline

```
User/Device/Events → Feature Extraction → FeatureVector (10 features)
                              ↓
                      WekaTrustModel (ML)
                              ↓
                    TrustScoringService → RiskLevel (HIGH/MEDIUM/LOW)
                              ↓
              PolicyEnforcementService → AccessDecision (DENY/STEP_UP/ALLOW)
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 4.0.0, Java 21
- **Security**: Spring Security 6.x
- **Database**: PostgreSQL 15 with Spring Data JPA
- **ML**: Weka 3.8.0 (Random Forest)
- **API Docs**: SpringDoc OpenAPI 2.3.0
- **Monitoring**: Spring Boot Actuator

### Frontend
- **Framework**: React 19.2.0 with TypeScript
- **Build Tool**: Vite 7.2.4
- **HTTP Client**: Axios 1.7.2
- **Charts**: Recharts 2.12.7

### DevOps
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven
- **Database**: PostgreSQL in Docker

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+ (for local development)
- Node.js 18+ (for frontend development)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/zero-trust-engine.git
cd zero-trust-engine
```

2. **Start all services**
```bash
docker compose up --build
```

This will start:
- PostgreSQL database on port 5433
- Backend API on port 8080
- Frontend dashboard on port 3000

3. **Access the application**
- Dashboard: http://localhost:3000
- API Documentation: http://localhost:8080/swagger-ui.html
- Actuator Metrics: http://localhost:8080/actuator

### First Time Setup

1. **Train the ML model** (via Dashboard or API):
```bash
curl -X POST "http://localhost:8080/api/admin/train?samples=1000"
```

2. **Evaluate the model**:
```bash
curl "http://localhost:8080/api/admin/evaluate?samples=500"
```

3. **View system metrics**:
```bash
curl "http://localhost:8080/api/metrics/system-stats"
```

## 📚 API Documentation

### Trust Score Endpoints

#### Get Current Trust Score
```http
GET /api/trust-score/{userId}
```

#### Get Risk History
```http
GET /api/risk-history/{userId}
```

### Admin Endpoints

#### Train Model
```http
POST /api/admin/train?samples=1000
```

#### Evaluate Model
```http
GET /api/admin/evaluate?samples=500
```

#### Get Confusion Metrics
```http
GET /api/admin/confusion-metrics?samples=500
```

### Metrics Endpoints

#### System Statistics
```http
GET /api/metrics/system-stats
```

#### Risk Distribution
```http
GET /api/metrics/risk-distribution
```

#### False Positive Rate
```http
GET /api/metrics/false-positive-rate
```

**Full API documentation available at**: http://localhost:8080/swagger-ui.html

## 🤖 ML Model

### Feature Vector (10 Features)

| Feature | Description | Type |
|---------|-------------|------|
| `failedLoginRate` | Ratio of failed login attempts | Behavioral |
| `nightAccessRate` | Ratio of access during night hours (10PM-6AM) | Behavioral |
| `loginFrequency24h` | Number of logins in last 24 hours | Behavioral |
| `avgDeviceRisk` | Average risk score across user's devices | Device Posture |
| `unpatchedDeviceRatio` | Ratio of unpatched devices | Device Posture |
| `antivirusDisabledRatio` | Ratio of devices with disabled antivirus | Device Posture |
| `networkRiskScore` | Risk score based on network type | Contextual |
| `locationChangeScore` | Score based on location changes | Contextual |
| `timeAnomalyScore` | Score based on time-based anomalies | Contextual |
| `secondsSinceLastLogin` | Time since last successful login | Account State |

### Model Architecture
- **Algorithm**: Random Forest (100 trees)
- **Training**: Supervised learning with synthetic labeled data
- **Output**: Trust score (0-100)
- **Risk Mapping**:
  - < 40: HIGH risk → DENY access
  - 40-70: MEDIUM risk → STEP_UP authentication
  - > 70: LOW risk → ALLOW access

### Performance Metrics
- Mean Absolute Error (MAE)
- Root Mean Squared Error (RMSE)
- Correlation Coefficient
- Confusion Matrix (TP, TN, FP, FN)
- False Positive/Negative Rates

## 📊 Dashboard

The React dashboard provides:

1. **Overview Tab**:
   - Total users and risk distribution
   - Average trust score display
   - Risk distribution pie chart
   - Real-time updates every 10 seconds

2. **Model Management Tab**:
   - Model training interface
   - Evaluation metrics display
   - Confusion matrix visualization
   - Model info and status

## 📁 Project Structure

```
zero-trust-engine/
├── backend/
│   ├── src/main/java/com/zerotrust/backend/
│   │   ├── config/          # OpenAPI, Security configs
│   │   ├── dto/             # FeatureVector, DTOs
│   │   ├── entities/        # User, Device, AccessEvent
│   │   ├── enums/           # RiskLevel, AccessDecision
│   │   ├── ml/              # SyntheticDataGenerator, Weka
│   │   ├── repositories/    # JPA repositories
│   │   ├── security/        # TrustScoreFilter
│   │   ├── services/        # Trust scoring, ML services
│   │   │   ├── features/    # FeatureExtractionService
│   │   │   └── trust/       # TrustModel, WekaTrustModel
│   │   └── web/             # REST controllers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # Dashboard, ModelManagement
│   │   ├── services/        # API client
│   │   ├── App.tsx
│   │   └── App.css
│   └── package.json
├── docker-compose.yml
└── README.md
```

## 🔧 Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://postgres:5432/zerotrust
spring.datasource.username=postgres
spring.datasource.password=postgres

# Server
server.port=8080

# Actuator
management.endpoints.web.exposure.include=health,info,metrics

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
```

### Frontend Configuration
Edit `frontend/src/services/api.ts`:

```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 📈 Future Enhancements

- [ ] JWT Authentication
- [ ] WebSocket for real-time updates
- [ ] Anomaly detection algorithms
- [ ] Behavioral baseline computation
- [ ] Model drift detection
- [ ] Spring AI integration
- [ ] User drill-down views
- [ ] Historical trend analysis
- [ ] Configurable trust thresholds
- [ ] Email/Slack alerts for high-risk users

## 📝 License

MIT License

## 👥 Contributors

Sesame University - Secure Programming - ING5 SE

---

**Built with ❤️ using Spring Boot, React, and Machine Learning**
