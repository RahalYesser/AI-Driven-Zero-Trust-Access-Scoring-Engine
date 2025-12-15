import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { logout as apiLogout } from '../services/api';
import './UserStatus.css';

export default function UserStatus() {
  const { user, loading, logout: authLogout, refreshUserStatus } = useAuth();
  const navigate = useNavigate();
  const [refreshing, setRefreshing] = useState(false);
  const [showTooltip, setShowTooltip] = useState<string | null>(null);

  const InfoIcon = ({ metric, tooltip }: { metric: string; tooltip: string }) => (
    <span 
      className="info-icon"
      onMouseEnter={() => setShowTooltip(metric)}
      onMouseLeave={() => setShowTooltip(null)}
      title={tooltip}
      style={{ marginLeft: '8px', cursor: 'help' }}
    >
      ℹ️
      {showTooltip === metric && (
        <div className="tooltip" style={{ position: 'absolute', zIndex: 1000 }}>
          {tooltip}
        </div>
      )}
    </span>
  );

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
      {/* Explanation Banner */}
      <div className="risk-reference-card" style={{ marginBottom: '1.5rem' }}>
        <h3>👤 Your Trust Score Profile</h3>
        <p className="risk-reference-text">
          Your trust score is calculated in real-time based on behavioral patterns, device security posture, 
          and contextual factors. The ML model analyzes 10 features including login history, device health, 
          network context, and time-based anomalies to determine your risk level and access permissions.
        </p>
      </div>

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
            <h3>
              Trust Score Assessment
              <InfoIcon 
                metric="trustScore" 
                tooltip="Your current trust score (0-100) determined by ML model analysis of your recent behavior, device security, and access patterns. Scores are recalculated every 5 minutes and on each login."
              />
            </h3>
            <button onClick={handleRefresh} className="btn-refresh" disabled={refreshing}>
              {refreshing ? '⏳ Refreshing...' : '🔄 Refresh Score'}
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
              <h4>
                Risk Level: <span style={{ color: getRiskColor(user.riskLevel) }}>{user.riskLevel}</span>
                <InfoIcon 
                  metric="riskLevel" 
                  tooltip={`Risk classification based on trust score. LOW (≥75): Full access. MEDIUM (40-74): MFA may be required. HIGH (<40): Access restricted or blocked.`}
                />
              </h4>
              <p style={{ color: '#4a5568', lineHeight: '1.6' }}>
                {user.riskLevel === 'LOW' && 'Your account shows normal behavior patterns. You have full system access with standard authentication.'}
                {user.riskLevel === 'MEDIUM' && 'Your account shows some behavioral anomalies. Enhanced security measures (MFA, additional verification) may be required for sensitive operations.'}
                {user.riskLevel === 'HIGH' && 'Your account exhibits high-risk patterns (failed logins, unusual access times, insecure devices). Access is restricted and may require administrator intervention.'}
              </p>
            </div>
          </div>

          <div className="account-details">
            <h4>
              Account Security Details
              <InfoIcon 
                metric="accountDetails" 
                tooltip="Security configuration and authentication metrics for your account. These factors influence your trust score calculation."
              />
            </h4>
            <div className="detail-grid">
              <div className="detail-item">
                <span className="detail-label">
                  MFA Status:
                  <InfoIcon 
                    metric="mfa" 
                    tooltip="Multi-Factor Authentication status. Enabling MFA improves your trust score by adding an extra security layer beyond passwords."
                  />
                </span>
                <span className={`detail-value ${user.mfaEnabled ? 'enabled' : 'disabled'}`}>
                  {user.mfaEnabled ? '✓ Enabled' : '✗ Disabled'}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">
                  Account Status:
                  <InfoIcon 
                    metric="accountStatus" 
                    tooltip="Account lock status. Locked accounts typically result from excessive failed logins or HIGH risk classification. Contact admin to unlock."
                  />
                </span>
                <span className={`detail-value ${user.accountLocked ? 'locked' : 'active'}`}>
                  {user.accountLocked ? '🔒 Locked' : '🔓 Active'}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">
                  Failed Login Attempts:
                  <InfoIcon 
                    metric="failedLogins" 
                    tooltip="Number of recent failed login attempts. High failure rates significantly reduce trust scores and may indicate credential compromise or brute force attacks."
                  />
                </span>
                <span className="detail-value" style={{ 
                  color: user.failedLoginAttempts > 5 ? '#ef4444' : 
                         user.failedLoginAttempts > 2 ? '#f59e0b' : '#10b981' 
                }}>
                  {user.failedLoginAttempts}
                  {user.failedLoginAttempts > 5 && ' ⚠️'}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">
                  Last Login:
                  <InfoIcon 
                    metric="lastLogin" 
                    tooltip="Timestamp of your most recent successful authentication. Long periods of inactivity may affect trust scores."
                  />
                </span>
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
