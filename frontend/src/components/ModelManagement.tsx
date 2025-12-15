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
      <div className="section">
        <h2>Model Status</h2>
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
        <h2>Train Model</h2>
        <div className="train-controls">
          <label>
            Training Samples:
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
            {training ? 'Training...' : 'Train Model'}
          </button>
        </div>

        {trainingResult && (
          <div className="result-card">
            <h3>Training Result</h3>
            <p>
              <strong>Samples:</strong> {trainingResult.numSamples}
            </p>
            <p>
              <strong>Time:</strong> {trainingResult.trainingTimeMs}ms
            </p>
            <p>
              <strong>Timestamp:</strong> {new Date(trainingResult.timestamp).toLocaleString()}
            </p>
          </div>
        )}
      </div>

      <div className="section">
        <h2>Model Evaluation</h2>
        <button onClick={handleEvaluate} disabled={evaluating} className="btn-secondary">
          {evaluating ? 'Evaluating...' : 'Evaluate Model'}
        </button>

        {evaluationMetrics && (
          <div className="metrics-grid">
            <div className="metric-card">
              <h4>MAE</h4>
              <p className="metric-value">{evaluationMetrics.meanAbsoluteError.toFixed(3)}</p>
            </div>
            <div className="metric-card">
              <h4>RMSE</h4>
              <p className="metric-value">{evaluationMetrics.rootMeanSquaredError.toFixed(3)}</p>
            </div>
            <div className="metric-card">
              <h4>Correlation</h4>
              <p className="metric-value">{evaluationMetrics.correlationCoefficient.toFixed(3)}</p>
            </div>
            <div className="metric-card">
              <h4>Eval Time</h4>
              <p className="metric-value">{evaluationMetrics.evaluationTimeMs}ms</p>
            </div>
          </div>
        )}

        {confusionMetrics && (
          <div className="confusion-matrix">
            <h3>Confusion Matrix Metrics</h3>
            <div className="metrics-grid">
              <div className="metric-card">
                <h4>Accuracy</h4>
                <p className="metric-value">{(confusionMetrics.accuracy * 100).toFixed(1)}%</p>
              </div>
              <div className="metric-card">
                <h4>False Positive Rate</h4>
                <p className="metric-value error">
                  {(confusionMetrics.falsePositiveRate * 100).toFixed(2)}%
                </p>
              </div>
              <div className="metric-card">
                <h4>False Negative Rate</h4>
                <p className="metric-value warning">
                  {(confusionMetrics.falseNegativeRate * 100).toFixed(2)}%
                </p>
              </div>
              <div className="metric-card">
                <h4>True Positives</h4>
                <p className="metric-value">{confusionMetrics.truePositives}</p>
              </div>
            </div>
          </div>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}
    </div>
  );
}
