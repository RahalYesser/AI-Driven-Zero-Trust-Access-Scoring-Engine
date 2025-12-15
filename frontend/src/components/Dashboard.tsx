import { useState, useEffect } from 'react';
import { getDashboardStats, type DashboardStats } from '../services/api';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

const COLORS = {
  HIGH: '#ef4444',
  MEDIUM: '#f59e0b',
  LOW: '#10b981',
};

export default function Dashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showTooltip, setShowTooltip] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const res = await getDashboardStats();
      setDashboardData(res.data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 10000); // Refresh every 10s
    return () => clearInterval(interval);
  }, []);

  if (loading && !dashboardData) {
    return <div className="loading">Loading dashboard...</div>;
  }

  if (error) {
    return <div className="error">Error: {error}</div>;
  }

  const stats = dashboardData?.stats;
  const distribution = dashboardData?.distribution;
  const explanations = dashboardData?.explanations;

  const pieData = distribution
    ? [
        { 
          name: 'High Risk', 
          value: distribution.HIGH, 
          color: COLORS.HIGH,
          percentage: distribution.highPercentage 
        },
        { 
          name: 'Medium Risk', 
          value: distribution.MEDIUM, 
          color: COLORS.MEDIUM,
          percentage: distribution.mediumPercentage 
        },
        { 
          name: 'Low Risk', 
          value: distribution.LOW, 
          color: COLORS.LOW,
          percentage: distribution.lowPercentage 
        },
      ]
    : [];

  const InfoIcon = ({ metric }: { metric: string }) => (
    <span 
      className="info-icon"
      onMouseEnter={() => setShowTooltip(metric)}
      onMouseLeave={() => setShowTooltip(null)}
      title={explanations?.[metric]}
    >
      ℹ️
    </span>
  );

  return (
    <div className="dashboard">
      {/* Risk Level Reference Card */}
      <div className="risk-reference-card">
        <h3>📊 Risk Level Classification</h3>
        <p className="risk-reference-text">{explanations?.riskLevels}</p>
      </div>

      {/* Main Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-header">
            <h3>Total Users</h3>
            <InfoIcon metric="totalUsers" />
          </div>
          <p className="stat-value">{stats?.totalUsers || 0}</p>
          {showTooltip === 'totalUsers' && (
            <div className="tooltip">{explanations?.totalUsers}</div>
          )}
        </div>

        <div className="stat-card high-risk">
          <div className="stat-header">
            <h3>🚫 High Risk</h3>
            <InfoIcon metric="highRiskUsers" />
          </div>
          <p className="stat-value">{stats?.highRiskUsers || 0}</p>
          <p className="stat-percentage">{distribution?.highPercentage.toFixed(1)}%</p>
          {showTooltip === 'highRiskUsers' && (
            <div className="tooltip">{explanations?.highRiskUsers}</div>
          )}
        </div>

        <div className="stat-card medium-risk">
          <div className="stat-header">
            <h3>⚠️ Medium Risk</h3>
            <InfoIcon metric="mediumRiskUsers" />
          </div>
          <p className="stat-value">{stats?.mediumRiskUsers || 0}</p>
          <p className="stat-percentage">{distribution?.mediumPercentage.toFixed(1)}%</p>
          {showTooltip === 'mediumRiskUsers' && (
            <div className="tooltip">{explanations?.mediumRiskUsers}</div>
          )}
        </div>

        <div className="stat-card low-risk">
          <div className="stat-header">
            <h3>✅ Low Risk</h3>
            <InfoIcon metric="lowRiskUsers" />
          </div>
          <p className="stat-value">{stats?.lowRiskUsers || 0}</p>
          <p className="stat-percentage">{distribution?.lowPercentage.toFixed(1)}%</p>
          {showTooltip === 'lowRiskUsers' && (
            <div className="tooltip">{explanations?.lowRiskUsers}</div>
          )}
        </div>
      </div>

      {/* Charts Section */}
      <div className="charts-section">
        <div className="chart-card">
          <div className="stat-header">
            <h3>Average Trust Score</h3>
            <InfoIcon metric="averageTrustScore" />
          </div>
          <div className="score-display">
            <div
              className={`score-circle ${
                (stats?.averageTrustScore || 0) < 40
                  ? 'high-risk'
                  : (stats?.averageTrustScore || 0) < 75
                  ? 'medium-risk'
                  : 'low-risk'
              }`}
            >
              {stats?.averageTrustScore.toFixed(1) || '0'}
            </div>
            <p className="score-scale">Scale: 0-100</p>
          </div>
          {showTooltip === 'averageTrustScore' && (
            <div className="tooltip">{explanations?.averageTrustScore}</div>
          )}
        </div>

        <div className="chart-card">
          <h3>Risk Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percentage }) => `${name}: ${percentage}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip formatter={(value: number) => `${value} users`} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Info Section */}
      <div className="info-section">
        <div className="info-card">
          <div className="stat-header">
            <strong>Total Score Calculations</strong>
            <InfoIcon metric="totalScoreCalculations" />
          </div>
          <span className="info-value">{stats?.totalScoreCalculations || 0}</span>
          {showTooltip === 'totalScoreCalculations' && (
            <div className="tooltip">{explanations?.totalScoreCalculations}</div>
          )}
        </div>
        <p className="update-time">
          🔄 Last updated: {new Date().toLocaleTimeString()}
        </p>
      </div>
    </div>
  );
}
