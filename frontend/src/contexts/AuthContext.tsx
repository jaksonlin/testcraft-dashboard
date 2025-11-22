/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useEffect, useState } from 'react';
import { setAuthToken, type LoginResponse } from '../lib/api';

interface AuthState {
  user: { username: string; roles: string[] } | null;
  token: string | null;
  defaultPasswordInUse: boolean;
}

interface AuthContextValue extends AuthState {
  isAuthenticated: boolean;
  isAdmin: boolean;
  needsPasswordChange: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
   markPasswordChanged: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const STORAGE_KEY = 'testcraft-auth';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(() => {
    if (typeof window === 'undefined') {
      return { user: null, token: null, defaultPasswordInUse: false };
    }
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored) as LoginResponse;
        setAuthToken(parsed.token);
        return {
          user: { username: parsed.username, roles: parsed.roles },
          token: parsed.token,
          defaultPasswordInUse: parsed.defaultPasswordInUse ?? false,
        };
      }
    } catch {
      // ignore
    }
    return { user: null, token: null, defaultPasswordInUse: false };
  });

  useEffect(() => {
    if (state.token) {
      setAuthToken(state.token);
      window.localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          token: state.token,
          username: state.user?.username,
          roles: state.user?.roles ?? [],
          defaultPasswordInUse: state.defaultPasswordInUse,
        }),
      );
    } else {
      setAuthToken(null);
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }, [state]);

  const login = async (username: string, password: string) => {
    const response = await import('../lib/api').then(m => m.api.auth.login(username, password));
    setState({
      user: { username: response.username, roles: response.roles },
      token: response.token,
      defaultPasswordInUse: response.defaultPasswordInUse ?? false,
    });
  };

  const logout = () => {
    setState({ user: null, token: null, defaultPasswordInUse: false });
  };

  const markPasswordChanged = () => {
    setState(prev => ({
      ...prev,
      defaultPasswordInUse: false,
    }));
  };

  const value: AuthContextValue = {
    ...state,
    isAuthenticated: !!state.token,
    isAdmin: !!state.user?.roles.includes('ROLE_ADMIN'),
    needsPasswordChange: !!state.token && state.defaultPasswordInUse,
    login,
    logout,
    markPasswordChanged,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
