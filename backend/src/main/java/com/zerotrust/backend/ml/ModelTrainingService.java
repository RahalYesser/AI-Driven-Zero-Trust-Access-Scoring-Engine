package com.zerotrust.backend.ml;

import com.zerotrust.backend.services.trust.WekaTrustModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for training and managing ML models
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelTrainingService {

    private final WekaTrustModel trustModel;
    private static final String MODEL_DIR = "models";
    private static final String MODEL_FILE = "trust_model.model";

    /**
     * Train the model with synthetic data
     */
    public TrainingResult trainModel(int numSamples) throws Exception {
        log.info("Starting model training with {} samples", numSamples);
        long startTime = System.currentTimeMillis();

        // Generate synthetic data
        SyntheticDataGenerator generator = new SyntheticDataGenerator();
        Instances trainingData = generator.generateTrainingData(numSamples);

        log.info("Generated {} training instances", trainingData.numInstances());

        // Train the model
        trustModel.train(trainingData);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Model training completed in {} ms", duration);

        // Save the model
        saveModel();

        return TrainingResult.builder()
                .success(true)
                .numSamples(numSamples)
                .trainingTimeMs(duration)
                .timestamp(LocalDateTime.now())
                .modelPath(getModelPath())
                .build();
    }

    /**
     * Save trained model to disk
     */
    public void saveModel() throws Exception {
        ensureModelDirectoryExists();
        String modelPath = getModelPath();

        log.info("Saving model to {}", modelPath);
        // Save only the classifier, not the service bean
        SerializationHelper.write(modelPath, trustModel.getClassifier());
        log.info("Model saved successfully");
    }

    /**
     * Load model from disk
     */
    public void loadModel() throws Exception {
        String modelPath = getModelPath();
        File modelFile = new File(modelPath);

        if (!modelFile.exists()) {
            log.warn("Model file not found at {}. Model needs to be trained first.", modelPath);
            throw new IllegalStateException("Model not trained. Please train the model first.");
        }

        log.info("Loading model from {}", modelPath);
        // Load the classifier from disk (note: this doesn't update the current trustModel instance)
        Object loadedClassifier = SerializationHelper.read(modelPath);
        log.info("Model loaded successfully: {}", loadedClassifier.getClass().getSimpleName());
    }

    /**
     * Check if trained model exists
     */
    public boolean isModelTrained() {
        return new File(getModelPath()).exists();
    }

    /**
     * Get model file info
     */
    public ModelInfo getModelInfo() {
        File modelFile = new File(getModelPath());

        if (!modelFile.exists()) {
            return ModelInfo.builder()
                    .exists(false)
                    .message("Model not trained yet")
                    .build();
        }

        return ModelInfo.builder()
                .exists(true)
                .path(modelFile.getAbsolutePath())
                .sizeBytes(modelFile.length())
                .lastModified(new java.util.Date(modelFile.lastModified()).toString())
                .build();
    }

    /**
     * Create backup of current model
     */
    public void backupModel() throws Exception {
        String currentPath = getModelPath();
        File currentFile = new File(currentPath);

        if (!currentFile.exists()) {
            throw new IllegalStateException("No model to backup");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupPath = MODEL_DIR + "/trust_model_backup_" + timestamp + ".model";

        Files.copy(currentFile.toPath(), Paths.get(backupPath));
        log.info("Model backed up to {}", backupPath);
    }

    private void ensureModelDirectoryExists() throws Exception {
        Path modelDir = Paths.get(MODEL_DIR);
        if (!Files.exists(modelDir)) {
            Files.createDirectories(modelDir);
            log.info("Created model directory: {}", MODEL_DIR);
        }
    }

    private String getModelPath() {
        return MODEL_DIR + "/" + MODEL_FILE;
    }

    /**
     * Training result DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class TrainingResult {
        private boolean success;
        private int numSamples;
        private long trainingTimeMs;
        private LocalDateTime timestamp;
        private String modelPath;
        private String message;
    }

    /**
     * Model info DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class ModelInfo {
        private boolean exists;
        private String path;
        private long sizeBytes;
        private String lastModified;
        private String message;
    }
}
