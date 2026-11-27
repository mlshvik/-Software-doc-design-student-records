import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, onUnauthorized, tokenStorage } from '../api/client';
import type { CurrentUser, LoginResponse } from '../api/types';

interface AuthState {
  user: CurrentUser | null;
  /** true, поки перевіряється збережений токен під час старту. */
  loading: boolean;
  login: (username: string, password: string) => Promise<CurrentUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState<boolean>(() => tokenStorage.get() !== null);

  const logout = useCallback(() => {
    tokenStorage.set(null);
    setUser(null);
  }, []);

  useEffect(() => {
    onUnauthorized(logout);
    return () => onUnauthorized(null);
  }, [logout]);

  // Якщо токен збережено з попереднього входу – відновлюємо профіль.
  useEffect(() => {
    if (!tokenStorage.get()) return;
    api.get<CurrentUser>('/me')
      .then(setUser)
      .catch(() => tokenStorage.set(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const response = await api.post<LoginResponse>('/auth/login', { username, password });
    tokenStorage.set(response.token);
    setUser(response.user);
    return response.user;
  }, []);

  const value = useMemo(() => ({ user, loading, login, logout }), [user, loading, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth має використовуватися всередині AuthProvider');
  return ctx;
}

/** Домашня сторінка для ролі користувача. */
export function homePath(user: CurrentUser): string {
  switch (user.role) {
    case 'ADMIN':
      return '/admin';
    case 'TEACHER':
      return '/teacher';
    default:
      return '/student';
  }
}
