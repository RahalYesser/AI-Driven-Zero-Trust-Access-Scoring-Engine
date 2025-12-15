import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Dashboard from './Dashboard';
import ModelManagement from './ModelManagement';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'model'>('dashboard');
  const navigate = useNavigate();

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <div className="admin-title">
          <h1>🛡️ Zero Trust Admin Dashboard</h1>
          <p>AI-Driven Access Scoring Engine Management</p>
        </div>
        <button onClick={() => navigate('/user-status')} className="btn-back">
          ← Back to User Status
        </button>
      </div>

      <div className="admin-tabs">
        <button
          className={`tab ${activeTab === 'dashboard' ? 'active' : ''}`}
          onClick={() => setActiveTab('dashboard')}
        >
          📊 Dashboard
        </button>
        <button
          className={`tab ${activeTab === 'model' ? 'active' : ''}`}
          onClick={() => setActiveTab('model')}
        >
          🤖 Model Management
        </button>
      </div>

      <div className="admin-content">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'model' && <ModelManagement />}
      </div>
    </div>
  );
}
