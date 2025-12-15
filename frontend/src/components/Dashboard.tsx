import { useState, useEffect } from 'react';
import { getSystemStats, getRiskDistribution, type SystemStats } from '../services/api';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

const COLORS = {
  HIGH: '#ef4444',
  MEDIUM: '#f59e0b',
  LOW: '#10b981',
};

export default function Dashboard() {
  const [stats, setStats] = useState<SystemStats | null>(null);
  const [distribution, setDistribution] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [statsRes, distRes] = await Promise.all([
        getSystemStats(),
        getRiskDistribution(),
      ]);
      setStats(statsRes.data);
      setDistribution(distRes.data);
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

  if (loading && !stats) {
    return <div className="loading">Loading dashboard...</div>;
  }

  if (error) {
    return <div className="error">Error: {error}</div>;
  }

  const pieData = distribution
    ? [
        { name: 'High Risk', value: distribution.distribution.HIGH, color: COLORS.HIGH },
        { name: 'Medium Risk', value: distribution.distribution.MEDIUM, color: COLORS.MEDIUM },
        { name: 'Low Risk', value: distribution.distribution.LOW, color: COLORS.LOW },
      ]
    : [];

  return (
    <div className="dashboard">
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Users</h3>
          <p className="stat-value">{stats?.totalUsers || 0}</p>
        </div>
        <div className="stat-card high-risk">
          <h3>High Risk Users</h3>
          <p className="stat-value">{stats?.highRiskUsers || 0}</p>
        </div>
        <div className="stat-card medium-risk">
          <h3>Medium Risk Users</h3>
          <p className="stat-value">{stats?.mediumRiskUsers || 0}</p>
        </div>
        <div className="stat-card low-risk">
          <h3>Low Risk Users</h3>
          <p className="stat-value">{stats?.lowRiskUsers || 0}</p>
        </div>
      </div>

      <div className="charts-section">
        <div className="chart-card">
          <h3>Average Trust Score</h3>
          <div className="score-display">
            <div
              className={`score-circle ${
                (stats?.averageTrustScore || 0) < 40
                  ? 'high-risk'
                  : (stats?.averageTrustScore || 0) < 70
                  ? 'medium-risk'
                  : 'low-risk'
              }`}
            >
              {stats?.averageTrustScore.toFixed(1) || '0'}
            </div>
          </div>
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
                label={({ name, percent }) => `${name}: ${((percent || 0) * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="info-section">
        <p>
          <strong>Total Score Calculations:</strong> {stats?.totalScoreCalculations || 0}
        </p>
        <p className="update-time">Last updated: {new Date().toLocaleTimeString()}</p>
      </div>
    </div>
  );
}
