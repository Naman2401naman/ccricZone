import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { apiRequest, getApiBase } from '../lib/api';

const STORAGE_KEY = 'criczone.react.auth';
const LEGACY_TOKEN_KEY = 'token';
const LEGACY_USER_KEY = 'user';

const AuthContext = createContext(null);

const readStoredAuth = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      const legacyToken = localStorage.getItem(LEGACY_TOKEN_KEY) || '';
      const legacyUserRaw = localStorage.getItem(LEGACY_USER_KEY);
      let legacyUser = null;
      if (legacyUserRaw) {
        try {
          legacyUser = JSON.parse(legacyUserRaw);
        } catch {
          legacyUser = null;
        }
      }
      return { token: legacyToken, user: legacyUser };
    }
    const parsed = JSON.parse(raw);
    return {
      token: typeof parsed.token === 'string' ? parsed.token : '',
      user: parsed.user || null
    };
  } catch {
    return { token: '', user: null };
  }
};

const writeStoredAuth = (token, user) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user }));
  if (token) {
    localStorage.setItem(LEGACY_TOKEN_KEY, token);
    localStorage.setItem(LEGACY_USER_KEY, JSON.stringify(user || {}));
  }
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState('');
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);
  const [apiBase] = useState(getApiBase());

  useEffect(() => {
    const stored = readStoredAuth();
    setToken(stored.token);
    setUser(stored.user);
    setReady(true);
  }, []);

  useEffect(() => {
    if (!ready) return;
    writeStoredAuth(token, user);
  }, [token, user, ready]);

  const clearSession = () => {
    setToken('');
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(LEGACY_TOKEN_KEY);
    localStorage.removeItem(LEGACY_USER_KEY);
  };

  const refreshProfile = async (incomingToken = token) => {
    if (!incomingToken) return null;
    const payload = await apiRequest('/users/profile', { token: incomingToken });
    const currentUser = payload.user || null;
    setUser(currentUser);
    return currentUser;
  };

  const signIn = async (email, password) => {
    const payload = await apiRequest('/users/login', {
      method: 'POST',
      body: { email, password }
    });
    const nextToken = payload.token || '';
    const nextUser = payload.user || null;
    setToken(nextToken);
    setUser(nextUser);
    return payload;
  };

  const signUp = async (payloadBody) => {
    let payload;
    try {
      payload = await apiRequest('/users/signup', {
        method: 'POST',
        body: payloadBody
      });
    } catch (error) {
      if (!/not found|404/i.test(error.message || '')) throw error;
      payload = await apiRequest('/users/register', {
        method: 'POST',
        body: payloadBody
      });
    }
    const nextToken = payload.token || '';
    const nextUser = payload.user || null;
    setToken(nextToken);
    setUser(nextUser);
    return payload;
  };

  const updateProfile = async (payloadBody) => {
    const payload = await apiRequest('/users/profile', {
      method: 'PUT',
      token,
      body: payloadBody
    });
    const nextUser = payload.user || null;
    setUser(nextUser);
    return payload;
  };

  const request = (path, options = {}) => apiRequest(path, { ...options, token });

  const value = useMemo(
    () => ({
      apiBase,
      token,
      user,
      ready,
      isAuthenticated: Boolean(token),
      clearSession,
      refreshProfile,
      signIn,
      signUp,
      updateProfile,
      request
    }),
    [apiBase, token, user, ready]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
