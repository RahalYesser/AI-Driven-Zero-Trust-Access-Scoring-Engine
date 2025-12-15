package com.zerotrust.backend.ml;

import com.zerotrust.backend.services.trust.WekaTrustModel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for evaluating ML model performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelEvaluationService {

    private final WekaTrustModel trustModel;

    /**
     * Evaluate model with test data
     */
    public EvaluationMetrics evaluateModel(int testSamples) throws Exception {
        log.info("Starting model evaluation with {} test samples", testSamples);
        long startTime = System.currentTimeMillis();

        // Generate test data
        SyntheticDataGenerator generator = new SyntheticDataGenerator(System.currentTimeMillis());
        Instances testData = generator.generateTrainingData(testSamples);

        // Perform evaluation
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(trustModel.getClassifier(), testData);

        long duration = System.currentTimeMillis() - startTime;

        // Extract metrics
        return buildMetrics(eval, testSamples, duration);
    }

    /**
     * Perform cross-validation
     */
    public EvaluationMetrics crossValidate(int numSamples, int folds) throws Exception {
        log.info("Starting {}-fold cross-validation with {} samples", folds, numSamples);
        long startTime = System.currentTimeMillis();

        // Generate data
        SyntheticDataGenerator generator = new SyntheticDataGenerator();
        Instances data = generator.generateTrainingData(numSamples);

        // Cross-validation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(trustModel.getClassifier(), data, folds, new java.util.Random(42));

        long duration = System.currentTimeMillis() - startTime;

        return buildMetrics(eval, numSamples, duration);
    }

    /**
     * Calculate false positive and false negative rates
     */
    public ConfusionMetrics calculateConfusionMetrics(int testSamples) throws Exception {
        SyntheticDataGenerator generator = new SyntheticDataGenerator(System.currentTimeMillis());
        Instances testData = generator.generateTrainingData(testSamples);

        int truePositives = 0;  // High risk correctly identified
        int trueNegatives = 0;  // Low risk correctly identified
        int falsePositives = 0; // Low risk incorrectly marked as high
        int falseNegatives = 0; // High risk incorrectly marked as low

        double threshold = 40.0; // Risk threshold

        for (int i = 0; i < testData.numInstances(); i++) {
            double actualScore = testData.instance(i).classValue();

            // Predict
            testData.instance(i).setMissing(testData.classIndex());
            double predictedScore = trustModel.getClassifier().classifyInstance(testData.instance(i));

            boolean actualHighRisk = actualScore < threshold;
            boolean predictedHighRisk = predictedScore < threshold;

            if (actualHighRisk && predictedHighRisk) {
                truePositives++;
            } else if (!actualHighRisk && !predictedHighRisk) {
                trueNegatives++;
            } else if (!actualHighRisk && predictedHighRisk) {
                falsePositives++;
            } else if (actualHighRisk && !predictedHighRisk) {
                falseNegatives++;
            }
        }

        double falsePositiveRate = (double) falsePositives / (falsePositives + trueNegatives);
        double falseNegativeRate = (double) falseNegatives / (falseNegatives + truePositives);
        double accuracy = (double) (truePositives + trueNegatives) / testSamples;

        return ConfusionMetrics.builder()
                .truePositives(truePositives)
                .trueNegatives(trueNegatives)
                .falsePositives(falsePositives)
                .falseNegatives(falseNegatives)
                .falsePositiveRate(falsePositiveRate)
                .falseNegativeRate(falseNegativeRate)
                .accuracy(accuracy)
                .threshold(threshold)
                .testSamples(testSamples)
                .build();
    }

    private EvaluationMetrics buildMetrics(Evaluation eval, int numSamples, long duration) throws Exception {
        // Calculate metrics
        double mae = eval.meanAbsoluteError();
        double rmse = eval.rootMeanSquaredError();
        double correlation = eval.correlationCoefficient();

        // For classification metrics, we need to threshold the scores
        // Let's use 40 as HIGH risk threshold (< 40), 70 as MEDIUM (40-70), >70 as LOW
        double[][] confusionMatrix = null;
        try {
            confusionMatrix = eval.confusionMatrix();
        } catch (Exception e) {
            log.warn("Could not extract confusion matrix: {}", e.getMessage());
        }

        Map<String, Object> additionalMetrics = new HashMap<>();
        additionalMetrics.put("meanAbsoluteError", mae);
        additionalMetrics.put("rootMeanSquaredError", rmse);
        additionalMetrics.put("correlationCoefficient", correlation);

        return EvaluationMetrics.builder()
                .accuracy(1.0 - (mae / 100.0)) // Approximation for regression
                .meanAbsoluteError(mae)
                .rootMeanSquaredError(rmse)
                .correlationCoefficient(correlation)
                .numSamples(numSamples)
                .evaluationTimeMs(duration)
                .timestamp(LocalDateTime.now())
                .summary(eval.toSummaryString())
                .additionalMetrics(additionalMetrics)
                .build();
    }

    /**
     * Evaluation metrics DTO
     */
    @Builder
    @Data
    public static class EvaluationMetrics {
        private double accuracy;
        private double precision;
        private double recall;
        private double f1Score;
        private double meanAbsoluteError;
        private double rootMeanSquaredError;
        private double correlationCoefficient;
        private int numSamples;
        private long evaluationTimeMs;
        private LocalDateTime timestamp;
        private String summary;
        private Map<String, Object> additionalMetrics;
    }

    /**
     * Confusion metrics DTO
     */
    @Builder
    @Data
    public static class ConfusionMetrics {
        private int truePositives;
        private int trueNegatives;
        private int falsePositives;
        private int falseNegatives;
        private double falsePositiveRate;
        private double falseNegativeRate;
        private double accuracy;
        private double threshold;
        private int testSamples;
    }
}
