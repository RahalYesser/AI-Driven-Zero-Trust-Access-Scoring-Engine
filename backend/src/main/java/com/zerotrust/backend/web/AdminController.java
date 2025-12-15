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
    @Operation(summary = "Train ML Model", description = "Train the Random Forest model with synthetic training data")
    public ResponseEntity<?> trainModel(
            @Parameter(description = "Number of training samples") @RequestParam(defaultValue = "1000") int samples) {
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
    @Operation(summary = "Evaluate Model", description = "Evaluate model performance with test data")
    public ResponseEntity<?> evaluateModel(
            @Parameter(description = "Number of test samples") @RequestParam(defaultValue = "500") int samples) {
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
    public ResponseEntity<?> getConfusionMetrics(@RequestParam(defaultValue = "500") int samples) {
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
