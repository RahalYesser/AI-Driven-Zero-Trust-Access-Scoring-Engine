import { useState, useEffect } from 'react';
import { getDashboardStats, getSystemHealth, type DashboardStats, type SystemHealth } from '../services/api';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

const COLORS = {
  HIGH: '#ef4444',
  MEDIUM: '#f59e0b',
  LOW: '#10b981',
};

export default function Dashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardStats | null>(null);
  const [systemHealth, setSystemHealth] = useState<SystemHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showTooltip, setShowTooltip] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [dashRes, healthRes] = await Promise.all([
        getDashboardStats(),
        getSystemHealth()
      ]);
      setDashboardData(dashRes.data);
      setSystemHealth(healthRes.data);
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
      {/* Enhanced Dashboard Introduction */}
      <div className="risk-reference-card">
        <h3>📊 Zero-Trust Access Dashboard</h3>
        <p className="risk-reference-text">
          This dashboard provides real-time visibility into user trust scores and risk classifications. 
          The ML model continuously evaluates users based on 10 behavioral and contextual features including 
          login patterns, device security, network context, and historical activity. Risk levels determine 
          access policies: <strong>LOW (&ge;75)</strong> = Full Access, <strong>MEDIUM (40-74)</strong> = MFA Required, 
          <strong>HIGH (&lt;40)</strong> = Access Blocked.
        </p>
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

      {/* System Health Metrics */}
      {systemHealth && (
        <div className="section" style={{ marginTop: '2rem' }}>
          <h2>
            🖥️ System Health & Performance
            <span 
              className="info-icon"
              onMouseEnter={() => setShowTooltip('systemHealth')}
              onMouseLeave={() => setShowTooltip(null)}
              style={{ marginLeft: '8px' }}
            >
              ℹ️
              {showTooltip === 'systemHealth' && (
                <div className="tooltip">
                  Real-time JVM memory usage, CPU information, and application status. 
                  Memory usage shows heap allocation, while system info displays hardware capabilities.
                </div>
              )}
            </span>
          </h2>
          
          <div className="stats-grid">
            {/* JVM Memory Usage */}
            <div className="stat-card">
              <div className="stat-header">
                <h3>💾 JVM Memory</h3>
                <span 
                  className="info-icon"
                  onMouseEnter={() => setShowTooltip('memory')}
                  onMouseLeave={() => setShowTooltip(null)}
                >
                  ℹ️
                  {showTooltip === 'memory' && (
                    <div className="tooltip">
                      Java Virtual Machine heap memory usage. Shows allocated memory out of maximum available. 
                      High usage (&gt;80%) may indicate need for garbage collection or memory tuning.
                    </div>
                  )}
                </span>
              </div>
              <p className="stat-value" style={{ 
                color: systemHealth.memory.usagePercentage > 80 ? '#ef4444' : 
                       systemHealth.memory.usagePercentage > 60 ? '#f59e0b' : '#10b981' 
              }}>
                {systemHealth.memory.usagePercentage}%
              </p>
              <p style={{ color: '#718096', fontSize: '0.85rem', marginTop: '0.5rem' }}>
                {systemHealth.memory.usedMB} MB / {systemHealth.memory.maxMB} MB
              </p>
              <p style={{ color: '#a0aec0', fontSize: '0.75rem' }}>
                Free: {systemHealth.memory.freeMB} MB
              </p>
            </div>

            {/* CPU Cores */}
            <div className="stat-card">
              <div className="stat-header">
                <h3>🔧 CPU Cores</h3>
                <span 
                  className="info-icon"
                  onMouseEnter={() => setShowTooltip('cpu')}
                  onMouseLeave={() => setShowTooltip(null)}
                >
                  ℹ️
                  {showTooltip === 'cpu' && (
                    <div className="tooltip">
                      Number of available processor cores for parallel task execution. 
                      More cores = better performance for concurrent ML predictions and API requests.
                    </div>
                  )}
                </span>
              </div>
              <p className="stat-value">{systemHealth.system.availableProcessors}</p>
              <p style={{ color: '#718096', fontSize: '0.85rem', marginTop: '0.5rem' }}>
                Processors
              </p>
              <p style={{ color: '#a0aec0', fontSize: '0.75rem' }}>
                {systemHealth.system.osArch}
              </p>
            </div>

            {/* Application Status */}
            <div className="stat-card">
              <div className="stat-header">
                <h3>✅ App Status</h3>
                <span 
                  className="info-icon"
                  onMouseEnter={() => setShowTooltip('appStatus')}
                  onMouseLeave={() => setShowTooltip(null)}
                >
                  ℹ️
                  {showTooltip === 'appStatus' && (
                    <div className="tooltip">
                      Application health status. UP = fully operational and ready to process requests. 
                      DOWN would indicate critical service failure.
                    </div>
                  )}
                </span>
              </div>
              <p className="stat-value" style={{ color: '#10b981' }}>
                {systemHealth.application.status}
              </p>
              <p style={{ color: '#718096', fontSize: '0.85rem', marginTop: '0.5rem' }}>
                Java {systemHealth.system.javaVersion}
              </p>
              <p style={{ color: '#a0aec0', fontSize: '0.75rem' }}>
                {systemHealth.system.osName}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
