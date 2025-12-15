import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Admin credentials for Basic Auth (backward compatibility)
const BASIC_AUTH_USERNAME = 'user1@company.com';
const BASIC_AUTH_PASSWORD = 'Password123!';

// Create axios instance for admin operations (with Basic Auth)
export const adminApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  auth: {
    username: BASIC_AUTH_USERNAME,
    password: BASIC_AUTH_PASSWORD,
  },
  withCredentials: true,
});

// Create axios instance for user operations (with JWT)
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// JWT token management
export const setAuthToken = (token: string | null) => {
  console.log('setAuthToken called with:', token ? token.substring(0, 20) + '...' : 'null');
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    localStorage.setItem('jwt_token', token);
    console.log('Token set in axios headers and localStorage');
  } else {
    delete api.defaults.headers.common['Authorization'];
    localStorage.removeItem('jwt_token');
    console.log('Token removed from axios headers and localStorage');
  }
};

// Initialize token from localStorage on app load
const savedToken = localStorage.getItem('jwt_token');
if (savedToken) {
  setAuthToken(savedToken);
}

// Response interceptor to handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.log('API interceptor caught error:', error.response?.status, error.config?.url);
    if (error.response?.status === 401) {
      console.log('401 Unauthorized, clearing token');
      setAuthToken(null);
      // Only redirect if not already on login page
      if (!window.location.pathname.includes('/login')) {
        console.log('Redirecting to login page');
        window.location.href = '/login';
      } else {
        console.log('Already on login page, not redirecting');
      }
    }
    return Promise.reject(error);
  }
);

export interface TrustScore {
  score: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  calculatedAt: string;
}

export interface RiskHistory {
  id: string;
  score: number;
  level: 'LOW' | 'MEDIUM' | 'HIGH';
  calculatedAt: string;
}

export interface SystemStats {
  totalUsers: number;
  highRiskUsers: number;
  mediumRiskUsers: number;
  lowRiskUsers: number;
  averageTrustScore: number;
  totalScoreCalculations: number;
}

export interface DashboardStats {
  stats: SystemStats;
  distribution: {
    HIGH: number;
    MEDIUM: number;
    LOW: number;
    highPercentage: number;
    mediumPercentage: number;
    lowPercentage: number;
  };
  explanations: {
    [key: string]: string;
  };
}

export interface ModelInfo {
  exists: boolean;
  path?: string;
  sizeBytes?: number;
  lastModified?: string;
  message?: string;
}

export interface TrainingResult {
  success: boolean;
  numSamples: number;
  trainingTimeMs: number;
  timestamp: string;
  modelPath: string;
}

export interface EvaluationMetrics {
  accuracy: number;
  meanAbsoluteError: number;
  rootMeanSquaredError: number;
  correlationCoefficient: number;
  numSamples: number;
  evaluationTimeMs: number;
}

export interface ConfusionMetrics {
  truePositives: number;
  trueNegatives: number;
  falsePositives: number;
  falseNegatives: number;
  falsePositiveRate: number;
  falseNegativeRate: number;
  accuracy: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token?: string;
  email: string;
  role: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  trustScore: number;
  decision: 'ALLOW' | 'REQUIRE_MFA' | 'BLOCKED';
  message: string;
  mfaRequired: boolean;
  accountLocked: boolean;
}

export interface UserStatusResponse {
  userId: string;
  email: string;
  role: string;
  trustScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  mfaEnabled: boolean;
  accountLocked: boolean;
  failedLoginAttempts: number;
  lastLoginAt: string;
  message: string;
}

export interface SystemHealth {
  memory: {
    usedMB: number;
    freeMB: number;
    totalMB: number;
    maxMB: number;
    usagePercentage: number;
  };
  system: {
    availableProcessors: number;
    javaVersion: string;
    osName: string;
    osArch: string;
  };
  application: {
    status: string;
    totalUsers: number;
    totalScoreCalculations: number;
    timestamp: string;
  };
}

// API Functions
// Get comprehensive dashboard stats with explanations
export const getDashboardStats = () =>
  adminApi.get<DashboardStats>('/metrics/dashboard');

// Get system health metrics
export const getSystemHealth = () =>
  adminApi.get<SystemHealth>('/metrics/system-health');

// Get risk history for a specific user
export const getRiskHistory = (userId: string) =>
  api.get<RiskHistory[]>(`/risk-history/${userId}`);

export const getModelInfo = () =>
  adminApi.get<ModelInfo>('/admin/model-info');

export const trainModel = (samples: number = 1000) =>
  adminApi.post<TrainingResult>(`/admin/train?samples=${samples}`);

export const evaluateModel = (samples: number = 500) =>
  adminApi.get<EvaluationMetrics>(`/admin/evaluate?samples=${samples}`);

export const getConfusionMetrics = (samples: number = 500) =>
  adminApi.get<ConfusionMetrics>(`/admin/confusion-metrics?samples=${samples}`);

// Authentication API Functions
export const login = (credentials: LoginRequest) => {
  console.log('Calling login API with email:', credentials.email);
  return axios.post<LoginResponse>(`${API_BASE_URL}/auth/login`, credentials);
};

export const logout = () => {
  setAuthToken(null);
  return api.post('/auth/logout');
};

export const getUserStatus = () => {
  console.log('Calling getUserStatus API');
  console.log('Current Authorization header:', api.defaults.headers.common['Authorization']);
  return api.get<UserStatusResponse>('/auth/user-status');
};

// Admin API Functions (using Basic Auth)
export const unlockUser = (userId: string) =>
  adminApi.post(`/admin/unlock-user/${userId}`);

export const getAllUsers = () =>
  adminApi.get('/admin/users');
