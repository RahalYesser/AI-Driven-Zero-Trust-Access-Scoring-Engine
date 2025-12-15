import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { setAuthToken, getUserStatus, type UserStatusResponse } from '../services/api';

interface AuthContextType {
  isAuthenticated: boolean;
  user: UserStatusResponse | null;
  loading: boolean;
  login: (token: string) => Promise<void>;
  logout: () => void;
  refreshUserStatus: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<UserStatusResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const refreshUserStatus = async () => {
    console.log('refreshUserStatus called');
    try {
      const response = await getUserStatus();
      console.log('getUserStatus response:', response.data);
      setUser(response.data);
      setIsAuthenticated(true);
      console.log('User status updated successfully');
    } catch (error) {
      console.error('refreshUserStatus error:', error);
      setIsAuthenticated(false);
      setUser(null);
      setAuthToken(null);
      throw error; // Re-throw so caller can handle
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      setAuthToken(token);
      refreshUserStatus().finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (token: string) => {
    console.log('AuthContext.login called with token:', token.substring(0, 20) + '...');
    setAuthToken(token);
    setIsAuthenticated(true);
    console.log('Calling refreshUserStatus...');
    try {
      await refreshUserStatus();
      console.log('User status refreshed successfully');
    } catch (error) {
      console.error('Failed to refresh user status after login:', error);
      // Don't clear auth on first refresh failure - token might need time
    }
  };

  const logout = () => {
    setAuthToken(null);
    setIsAuthenticated(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, loading, login, logout, refreshUserStatus }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
