import React, { createContext, useState, useEffect, useCallback } from 'react';
import { login as apiLogin, getMe } from '../api/authApi';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // Au montage, on vérifie si un token est présent et valide pour récupérer les infos de l'utilisateur
  useEffect(() => {
    const token = localStorage.getItem('access_token');
    if (!token) {
      setLoading(false);
      return;
    }
    getMe()
      .then((data) => setUser(data))
      .catch(() => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      })
      .finally(() => setLoading(false));
  }, []);

  // Fonction de connexion : appelle l'API, stocke les tokens et les infos utilisateur
  const login = useCallback(async (mail, password) => {
    const data = await apiLogin(mail, password);
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    setUser(data.user);
  }, []);

  // Fonction de déconnexion : supprime les tokens et les infos utilisateur
  const logout = useCallback(() => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
