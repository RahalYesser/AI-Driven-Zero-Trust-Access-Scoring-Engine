import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { logout as apiLogout } from '../services/api';
import './UserStatus.css';

export default function UserStatus() {
  const { user, loading, logout: authLogout, refreshUserStatus } = useAuth();
  const navigate = useNavigate();
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (!loading && !user) {
      navigate('/login');
    }
  }, [user, loading, navigate]);

  const handleLogout = async () => {
    try {
      await apiLogout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      authLogout();
      navigate('/login');
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await refreshUserStatus();
    setRefreshing(false);
  };

  const handleAdminDashboard = () => {
    navigate('/admin');
  };

  if (loading || !user) {
    return (
      <div className="user-status-container">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case 'HIGH': return '#ef4444';
      case 'MEDIUM': return '#f59e0b';
      case 'LOW': return '#10b981';
      default: return '#6b7280';
    }
  };

  const getRiskEmoji = (risk: string) => {
    switch (risk) {
      case 'HIGH': return '🚫';
      case 'MEDIUM': return '⚠️';
      case 'LOW': return '✅';
      default: return '❓';
    }
  };

  return (
    <div className="user-status-container">
      <div className="user-status-card">
        <div className="user-header">
          <div className="user-avatar">
            {user.email.charAt(0).toUpperCase()}
          </div>
          <div className="user-info">
            <h2>{user.email}</h2>
            <p className="user-role">{user.role}</p>
          </div>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>

        <div className="status-content">
          <div className="status-header">
            <h3>Trust Score Assessment</h3>
            <button onClick={handleRefresh} className="btn-refresh" disabled={refreshing}>
              {refreshing ? '🔄 Refreshing...' : '🔄 Refresh'}
            </button>
          </div>

          <div className="trust-score-display">
            <div className="score-circle" style={{ 
              borderColor: getRiskColor(user.riskLevel),
              boxShadow: `0 0 30px ${getRiskColor(user.riskLevel)}40`
            }}>
              <div className="score-value">{user.trustScore.toFixed(1)}</div>
              <div className="score-label">Trust Score</div>
            </div>
          </div>

          <div className="risk-level-card" style={{ borderLeftColor: getRiskColor(user.riskLevel) }}>
            <div className="risk-emoji">{getRiskEmoji(user.riskLevel)}</div>
            <div className="risk-content">
              <h4>Risk Level: <span style={{ color: getRiskColor(user.riskLevel) }}>{user.riskLevel}</span></h4>
              <p>
                {user.riskLevel === 'LOW' && 'Your account shows normal behavior. Full access granted.'}
                {user.riskLevel === 'MEDIUM' && 'Your account shows some anomalies. Enhanced security measures may apply.'}
                {user.riskLevel === 'HIGH' && 'Your account shows high-risk behavior. Access may be restricted.'}
              </p>
            </div>
          </div>

          <div className="account-details">
            <h4>Account Details</h4>
            <div className="detail-grid">
              <div className="detail-item">
                <span className="detail-label">MFA Status:</span>
                <span className={`detail-value ${user.mfaEnabled ? 'enabled' : 'disabled'}`}>
                  {user.mfaEnabled ? '✓ Enabled' : '✗ Disabled'}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Account Status:</span>
                <span className={`detail-value ${user.accountLocked ? 'locked' : 'active'}`}>
                  {user.accountLocked ? '🔒 Locked' : '🔓 Active'}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Failed Login Attempts:</span>
                <span className="detail-value">{user.failedLoginAttempts}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Last Login:</span>
                <span className="detail-value">
                  {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : 'N/A'}
                </span>
              </div>
            </div>
          </div>

          {user.accountLocked && (
            <div className="locked-notice">
              <span className="locked-icon">🔒</span>
              <p>Your account is locked. Please contact an administrator to unlock it.</p>
            </div>
          )}

          {user.role === 'ADMIN' && (
            <div className="admin-actions">
              <button onClick={handleAdminDashboard} className="btn-admin">
                🔧 Admin Dashboard
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
