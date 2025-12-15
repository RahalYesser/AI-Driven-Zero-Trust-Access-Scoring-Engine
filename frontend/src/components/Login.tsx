import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as apiLogin } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import './Login.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showMfa, setShowMfa] = useState(false);
  const [loginResponse, setLoginResponse] = useState<any>(null);
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    console.log('Login attempt:', { email });

    try {
      const response = await apiLogin({ email, password });
      const data = response.data;
      
      console.log('Login response:', data);
      setLoginResponse(data);

      // Handle different risk levels
      if (data.decision === 'BLOCKED') {
        console.log('Login BLOCKED');
        setError(data.message);
        setLoading(false);
      } else if (data.decision === 'REQUIRE_MFA') {
        console.log('Login REQUIRE_MFA');
        setShowMfa(true);
        setLoading(false);
      } else if (data.decision === 'ALLOW' && data.token) {
        console.log('Login ALLOW, token:', data.token.substring(0, 20) + '...');
        await authLogin(data.token);
        console.log('Auth login completed, navigating to user-status');
        navigate('/user-status');
      } else {
        console.error('Unexpected decision or missing token:', data);
        setError('Unexpected login response. Please try again.');
        setLoading(false);
      }
    } catch (err: any) {
      console.error('Login error:', err);
      console.error('Error response:', err.response?.data);
      setError(err.response?.data?.message || 'Login failed. Please try again.');
      setLoading(false);
    }
  };

  const handleMfaContinue = async () => {
    if (loginResponse?.token) {
      await authLogin(loginResponse.token);
      navigate('/user-status');
    }
  };

  if (showMfa) {
    return (
      <div className="login-container">
        <div className="login-card mfa-card">
          <div className="mfa-icon">🔐</div>
          <h2>Multi-Factor Authentication Required</h2>
          <p className="mfa-message">
            Your trust score indicates <strong>MEDIUM RISK</strong>. 
            Multi-factor authentication would normally be required here.
          </p>
          <div className="mfa-info">
            <p><strong>Trust Score:</strong> {loginResponse?.trustScore.toFixed(1)}</p>
            <p><strong>Risk Level:</strong> <span className="risk-medium">{loginResponse?.riskLevel}</span></p>
          </div>
          <div className="mfa-mock-notice">
            <p>⚠️ This is a <strong>mock MFA screen</strong> for demonstration purposes.</p>
            <p>In production, you would enter a 6-digit code from your authenticator app.</p>
          </div>
          <button onClick={handleMfaContinue} className="btn-primary">
            Continue to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>🛡️ Zero Trust Access</h1>
          <p>AI-Driven Security Authentication</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@company.com"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              disabled={loading}
            />
          </div>

          {error && (
            <div className="error-message">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Authenticating...' : 'Login'}
          </button>
        </form>

        <div className="login-footer">
          <p className="demo-credentials">
            <strong>Demo Credentials:</strong><br />
            user1@company.com / Password123!<br />
            user2@company.com / Password123!
          </p>
        </div>
      </div>
    </div>
  );
}
