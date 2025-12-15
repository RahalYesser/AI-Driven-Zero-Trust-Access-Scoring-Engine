import { useState, useEffect } from 'react';
import {
  getModelInfo,
  trainModel,
  evaluateModel,
  getConfusionMetrics,
  type ModelInfo,
  type TrainingResult,
  type EvaluationMetrics,
  type ConfusionMetrics,
} from '../services/api';

export default function ModelManagement() {
  const [modelInfo, setModelInfo] = useState<ModelInfo | null>(null);
  const [training, setTraining] = useState(false);
  const [evaluating, setEvaluating] = useState(false);
  const [trainingResult, setTrainingResult] = useState<TrainingResult | null>(null);
  const [evaluationMetrics, setEvaluationMetrics] = useState<EvaluationMetrics | null>(null);
  const [confusionMetrics, setConfusionMetrics] = useState<ConfusionMetrics | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [samples, setSamples] = useState(1000);
  const [showTooltip, setShowTooltip] = useState<string | null>(null);

  const InfoIcon = ({ metric, tooltip }: { metric: string; tooltip: string }) => (
    <span 
      className="info-icon"
      onMouseEnter={() => setShowTooltip(metric)}
      onMouseLeave={() => setShowTooltip(null)}
      title={tooltip}
      style={{ marginLeft: '8px' }}
    >
      ℹ️
      {showTooltip === metric && (
        <div className="tooltip">{tooltip}</div>
      )}
    </span>
  );

  const fetchModelInfo = async () => {
    try {
      const res = await getModelInfo();
      setModelInfo(res.data);
    } catch (err: any) {
      console.error('Failed to fetch model info:', err);
    }
  };

  useEffect(() => {
    fetchModelInfo();
  }, []);

  const handleTrain = async () => {
    try {
      setTraining(true);
      setError(null);
      const res = await trainModel(samples);
      setTrainingResult(res.data);
      await fetchModelInfo();
      alert('Model trained successfully!');
    } catch (err: any) {
      setError(err.message || 'Training failed');
    } finally {
      setTraining(false);
    }
  };

  const handleEvaluate = async () => {
    try {
      setEvaluating(true);
      setError(null);
      const [evalRes, confRes] = await Promise.all([
        evaluateModel(500),
        getConfusionMetrics(500),
      ]);
      setEvaluationMetrics(evalRes.data);
      setConfusionMetrics(confRes.data);
    } catch (err: any) {
      setError(err.message || 'Evaluation failed');
    } finally {
      setEvaluating(false);
    }
  };

  return (
    <div className="model-management">
      {/* Explanation Banner */}
      <div className="risk-reference-card">
        <h3>🤖 ML Model Management</h3>
        <p className="risk-reference-text">
          This section allows you to manage the Random Forest machine learning model that predicts user trust scores (0-100). 
          The model uses 10 behavioral and contextual features to assess risk levels. Training generates synthetic data with 
          balanced risk profiles, while evaluation tests the model's accuracy on fresh data.
        </p>
      </div>

      <div className="section">
        <h2>
          Model Status
          <InfoIcon 
            metric="modelStatus" 
            tooltip="Shows whether the ML model is currently trained and ready for predictions. A trained model is saved to disk and can make real-time trust score predictions."
          />
        </h2>
        <div className="model-info">
          {modelInfo ? (
            <>
              <p>
                <strong>Status:</strong>{' '}
                <span className={modelInfo.exists ? 'status-ok' : 'status-warning'}>
                  {modelInfo.exists ? '✓ Trained' : '⚠ Not Trained'}
                </span>
              </p>
              {modelInfo.exists && (
                <>
                  <p>
                    <strong>Path:</strong> {modelInfo.path}
                  </p>
                  <p>
                    <strong>Size:</strong> {((modelInfo.sizeBytes || 0) / 1024).toFixed(2)} KB
                  </p>
                  <p>
                    <strong>Last Modified:</strong> {modelInfo.lastModified}
                  </p>
                </>
              )}
              {modelInfo.message && <p>{modelInfo.message}</p>}
            </>
          ) : (
            <p>Loading model info...</p>
          )}
        </div>
      </div>

      <div className="section">
        <h2>
          🎓 Train Model
          <InfoIcon 
            metric="trainModel" 
            tooltip="Training creates synthetic user data with balanced LOW/MEDIUM/HIGH risk profiles, extracts 10 ML features (failed login rate, night access, device risk, etc.), and trains a Random Forest model with 100 decision trees. This typically takes 1-3 seconds."
          />
        </h2>
        <p style={{ color: '#4a5568', marginBottom: '1rem', lineHeight: '1.6' }}>
          Generate synthetic training data and build a new Random Forest model. The training process creates balanced 
          datasets representing different risk profiles and teaches the model to recognize patterns in user behavior, 
          device security posture, and contextual signals.
        </p>
        <div className="train-controls">
          <label>
            <strong>Training Samples:</strong>
            <InfoIcon 
              metric="trainingSamples" 
              tooltip="Number of synthetic user profiles to generate for training. More samples = better model accuracy but longer training time. Recommended: 1000-2000 samples for development, 5000+ for production."
            />
            <input
              type="number"
              value={samples}
              onChange={(e) => setSamples(Number(e.target.value))}
              min="100"
              max="10000"
              step="100"
            />
          </label>
          <button onClick={handleTrain} disabled={training} className="btn-primary">
            {training ? '⏳ Training...' : '🚀 Train Model'}
          </button>
        </div>

        {trainingResult && (
          <div className="result-card">
            <h3>✅ Training Completed Successfully</h3>
            <p>
              <strong>Samples Generated:</strong> {trainingResult.numSamples} 
              <span style={{ color: '#718096', fontSize: '0.9rem', marginLeft: '8px' }}>
                (Balanced LOW/MEDIUM/HIGH risk profiles)
              </span>
            </p>
            <p>
              <strong>Training Duration:</strong> {trainingResult.trainingTimeMs}ms
              <span style={{ color: '#718096', fontSize: '0.9rem', marginLeft: '8px' }}>
                (Random Forest with 100 trees)
              </span>
            </p>
            <p>
              <strong>Completed At:</strong> {new Date(trainingResult.timestamp).toLocaleString()}
            </p>
            <p>
              <strong>Model Saved To:</strong> <code>{trainingResult.modelPath}</code>
            </p>
          </div>
        )}
      </div>

      <div className="section">
        <h2>
          📊 Model Evaluation
          <InfoIcon 
            metric="evaluation" 
            tooltip="Tests the trained model on fresh synthetic data to measure prediction accuracy. Generates new test samples, compares predicted vs. actual trust scores, and computes error metrics (MAE, RMSE) and correlation."
          />
        </h2>
        <p style={{ color: '#4a5568', marginBottom: '1rem', lineHeight: '1.6' }}>
          Test the model's prediction accuracy on fresh, unseen data. This generates new synthetic samples and measures 
          how well the model predicts trust scores by comparing predictions against ground-truth labels.
        </p>
        <button onClick={handleEvaluate} disabled={evaluating} className="btn-secondary">
          {evaluating ? '⏳ Evaluating...' : '📈 Evaluate Model'}
        </button>

        {evaluationMetrics && (
          <div className="metrics-grid">
            <div className="metric-card">
              <h4>
                MAE
                <InfoIcon 
                  metric="mae" 
                  tooltip="Mean Absolute Error: Average difference between predicted and actual trust scores. Lower is better. Good MAE: <8, Excellent: <5. Represents how many points off predictions are on average."
                />
              </h4>
              <p className="metric-value">{evaluationMetrics.meanAbsoluteError.toFixed(3)}</p>
              <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                {evaluationMetrics.meanAbsoluteError < 5 ? '✅ Excellent' : 
                 evaluationMetrics.meanAbsoluteError < 8 ? '✓ Good' : '⚠ Needs Improvement'}
              </p>
            </div>
            <div className="metric-card">
              <h4>
                RMSE
                <InfoIcon 
                  metric="rmse" 
                  tooltip="Root Mean Squared Error: Emphasizes larger prediction errors more than MAE. Lower is better. Good RMSE: <10, Excellent: <7. Penalizes outliers more heavily."
                />
              </h4>
              <p className="metric-value">{evaluationMetrics.rootMeanSquaredError.toFixed(3)}</p>
              <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                {evaluationMetrics.rootMeanSquaredError < 7 ? '✅ Excellent' : 
                 evaluationMetrics.rootMeanSquaredError < 10 ? '✓ Good' : '⚠ Needs Improvement'}
              </p>
            </div>
            <div className="metric-card">
              <h4>
                Correlation
                <InfoIcon 
                  metric="correlation" 
                  tooltip="Pearson Correlation Coefficient: Measures how well predictions match actual scores (0-1 scale). Higher is better. Good: >0.85, Excellent: >0.90. Shows linear relationship strength."
                />
              </h4>
              <p className="metric-value">{evaluationMetrics.correlationCoefficient.toFixed(3)}</p>
              <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                {evaluationMetrics.correlationCoefficient > 0.90 ? '✅ Excellent' : 
                 evaluationMetrics.correlationCoefficient > 0.85 ? '✓ Good' : '⚠ Needs Improvement'}
              </p>
            </div>
            <div className="metric-card">
              <h4>
                Eval Time
                <InfoIcon 
                  metric="evalTime" 
                  tooltip="Time taken to evaluate the model on test samples. Includes synthetic data generation, feature extraction, and prediction. Typical: 200-500ms for 500 samples."
                />
              </h4>
              <p className="metric-value">{evaluationMetrics.evaluationTimeMs}ms</p>
              <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                {evaluationMetrics.numSamples} samples
              </p>
            </div>
          </div>
        )}

        {confusionMetrics && (
          <div className="confusion-matrix">
            <h3>
              🎯 Confusion Matrix Analysis
              <InfoIcon 
                metric="confusionMatrix" 
                tooltip="Analyzes classification accuracy by counting correct and incorrect risk level predictions. Uses threshold of 40 (HIGH risk cutoff) to classify users. Critical for understanding false alarms and missed threats."
              />
            </h3>
            <p style={{ color: '#4a5568', marginBottom: '1rem', fontSize: '0.9rem', lineHeight: '1.6' }}>
              Confusion matrix measures how well the model classifies HIGH-risk users (trust score &lt; 40) vs. non-HIGH-risk users. 
              It reveals false alarms (false positives) and missed threats (false negatives).
            </p>
            <div className="metrics-grid">
              <div className="metric-card">
                <h4>
                  Accuracy
                  <InfoIcon 
                    metric="accuracy" 
                    tooltip="Percentage of correct predictions (both HIGH and non-HIGH risk). Formula: (TP + TN) / Total. Target: >90% for production systems. Measures overall classification correctness."
                  />
                </h4>
                <p className="metric-value">{(confusionMetrics.accuracy * 100).toFixed(1)}%</p>
                <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                  {confusionMetrics.accuracy > 0.90 ? '✅ Excellent' : 
                   confusionMetrics.accuracy > 0.85 ? '✓ Good' : '⚠ Needs Improvement'}
                </p>
              </div>
              <div className="metric-card">
                <h4>
                  False Positive Rate
                  <InfoIcon 
                    metric="falsePositive" 
                    tooltip="Percentage of safe users incorrectly flagged as HIGH risk. Lower is better. Formula: FP / (FP + TN). High FPR causes user frustration from false alarms. Target: <5%."
                  />
                </h4>
                <p className="metric-value error">
                  {(confusionMetrics.falsePositiveRate * 100).toFixed(2)}%
                </p>
                <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                  {confusionMetrics.falsePositives} false alarms
                </p>
              </div>
              <div className="metric-card">
                <h4>
                  False Negative Rate
                  <InfoIcon 
                    metric="falseNegative" 
                    tooltip="Percentage of HIGH-risk users incorrectly classified as safe. Lower is better. Formula: FN / (FN + TP). High FNR is dangerous - allows threats through. Target: <10%."
                  />
                </h4>
                <p className="metric-value warning">
                  {(confusionMetrics.falseNegativeRate * 100).toFixed(2)}%
                </p>
                <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                  {confusionMetrics.falseNegatives} missed threats
                </p>
              </div>
              <div className="metric-card">
                <h4>
                  True Positives
                  <InfoIcon 
                    metric="truePositives" 
                    tooltip="Number of HIGH-risk users correctly identified. Paired with True Negatives (correctly identified safe users: {confusionMetrics.trueNegatives}), this measures model's detection capability."
                  />
                </h4>
                <p className="metric-value">{confusionMetrics.truePositives}</p>
                <p style={{ color: '#718096', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                  TN: {confusionMetrics.trueNegatives}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {error && <div className="error-message">❌ {error}</div>}
    </div>
  );
}
