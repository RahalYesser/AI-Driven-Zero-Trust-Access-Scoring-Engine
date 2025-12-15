package com.zerotrust.backend.web;

import com.zerotrust.backend.entities.User;
import com.zerotrust.backend.ml.ModelEvaluationService;
import com.zerotrust.backend.ml.ModelTrainingService;
import com.zerotrust.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin endpoints for ML model management
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "ML Model Management & Training APIs")
public class AdminController {

    private final ModelTrainingService trainingService;
    private final ModelEvaluationService evaluationService;
    private final UserRepository userRepository;

    /**
     * Train the ML model with synthetic data
     * POST /api/admin/train?samples=1000
     */
    @PostMapping("/train")
    @Operation(
        summary = "Train ML Model",
        description = """
            Train the Random Forest regression model using synthetic training data.
            
            **Training Process:**
            1. Generates balanced synthetic dataset (1/3 LOW, 1/3 MEDIUM, 1/3 HIGH risk)
            2. Creates 10-feature vectors with labeled trust scores (0-100)
            3. Trains Random Forest with 100 decision trees
            4. Saves model to disk at `models/trust_model.model`
            
            **Feature Generation:**
            - LOW RISK: failedLoginRate < 5%, secure devices, internal networks → Score 75-95
            - MEDIUM RISK: failedLoginRate 5-20%, moderate device risk → Score 40-74
            - HIGH RISK: failedLoginRate > 20%, insecure devices, TOR networks → Score 0-39
            
            **Model Configuration:**
            - Algorithm: Random Forest Regression
            - Trees: 100 (ensemble learning)
            - Features: 10 behavioral/device/contextual signals
            
            **Response:**
            Returns training metrics including sample count, duration, and model path.
            
            **Typical Training Time:** ~1-2 seconds for 1000 samples
            """
    )
    public ResponseEntity<?> trainModel(
            @Parameter(description = "Number of training samples (recommended: 1000-5000)") @RequestParam(defaultValue = "1000") int samples) {
        try {
            ModelTrainingService.TrainingResult result = trainingService.trainModel(samples);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Evaluate model performance
     * GET /api/admin/evaluate?samples=500
     */
    @GetMapping("/evaluate")
    @Operation(
        summary = "Evaluate Model Performance",
        description = """
            Evaluate the trained Random Forest model using fresh test data.
            
            **Evaluation Process:**
            1. Generates new synthetic test dataset (different seed from training)
            2. Makes predictions for all test samples
            3. Compares predictions vs. ground truth labels
            4. Calculates regression metrics
            
            **Metrics Returned:**
            - **Accuracy**: Approximated as `1 - (MAE/100)` → Target: > 90%
            - **MAE** (Mean Absolute Error): Average prediction error in points → Target: < 10
            - **RMSE** (Root Mean Squared Error): Penalizes large errors → Target: < 15
            - **Correlation Coefficient**: Prediction quality (0-1) → Target: > 0.85
            
            **Interpretation:**
            - MAE of 5.2 means predictions are typically within ±5 points of actual score
            - Correlation of 0.92 indicates strong predictive relationship
            
            **Use Cases:**
            - Model validation after training
            - Regression metric monitoring
            - Before deploying to production
            
            **Typical Evaluation Time:** ~0.5 seconds for 500 samples
            """
    )
    public ResponseEntity<?> evaluateModel(
            @Parameter(description = "Number of test samples (recommended: 500-1000)") @RequestParam(defaultValue = "500") int samples) {
        try {
            ModelEvaluationService.EvaluationMetrics metrics =
                    evaluationService.evaluateModel(samples);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Removed - use /evaluate endpoint for model validation

    /**
     * Get confusion matrix and false positive/negative rates
     * GET /api/admin/confusion-metrics?samples=500
     */
    @GetMapping("/confusion-metrics")
    @Operation(
        summary = "Get Confusion Matrix Metrics",
        description = """
            Calculate classification metrics by thresholding trust scores.
            
            **Threshold Logic:**
            - HIGH RISK: Score < 40 (should be blocked)
            - LOW RISK: Score ≥ 40 (should be allowed)
            
            **Confusion Matrix:**
            ```
                         Predicted
                      HIGH    LOW
            Actual HIGH  TP     FN
                   LOW   FP     TN
            ```
            
            **Metrics Returned:**
            - **True Positives (TP)**: High risk correctly identified → GOOD
            - **True Negatives (TN)**: Low risk correctly identified → GOOD
            - **False Positives (FP)**: Low risk incorrectly flagged as high → BAD (blocks legitimate users)
            - **False Negatives (FN)**: High risk missed → CRITICAL (security breach risk)
            
            **Rates:**
            - **False Positive Rate**: FP / (FP + TN) → Target: < 10% (minimize user friction)
            - **False Negative Rate**: FN / (FN + TP) → Target: < 5% (minimize security risk)
            - **Accuracy**: (TP + TN) / Total → Target: > 85%
            
            **Example:**
            - FPR = 7.4% means 7.4% of legitimate users are incorrectly blocked
            - FNR = 2.1% means 2.1% of attackers slip through (acceptable for Zero Trust)
            
            **Use Cases:**
            - Security vs. usability trade-off analysis
            - Threshold tuning (adjust 40 cutoff point)
            - Compliance reporting
            """
    )
    public ResponseEntity<?> getConfusionMetrics(
            @Parameter(description = "Number of test samples") @RequestParam(defaultValue = "500") int samples) {
        try {
            ModelEvaluationService.ConfusionMetrics metrics =
                    evaluationService.calculateConfusionMetrics(samples);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get model info
     * GET /api/admin/model-info
     */
    @GetMapping("/model-info")
    public ResponseEntity<?> getModelInfo() {
        ModelTrainingService.ModelInfo info = trainingService.getModelInfo();
        return ResponseEntity.ok(info);
    }

    // Removed redundant endpoints - model is auto-saved after training

    /**
     * Unlock a locked user account
     * POST /api/admin/unlock-user/{userId}
     */
    @PostMapping("/unlock-user/{userId}")
    @Operation(summary = "Unlock User Account", description = "Unlock a user account that was locked due to high risk or failed login attempts")
    public ResponseEntity<?> unlockUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                    "message", "User account unlocked successfully",
                    "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all users (for admin management)
     * GET /api/admin/users
     */
    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Get list of all users with their trust scores and status")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
