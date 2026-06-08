import { createContext, useState, useEffect, useCallback } from 'react';
import { login as apiLogin } from '../api/authApi';
import { getMe } from '../api/apiCalls';

/**
 * Le fichier AuthContext est responsable de la gestion de l'état d'authentification de l'utilisateur
 * Il fournit des fonctions pour se connecter, se déconnecter et rafraîchir les informations de l'utilisateur
 */

export const AuthContext = createContext(null);

// Le composant AuthProvider enveloppe l'application et fournit le contexte d'authentification à tous les composants enfants
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

  // Fonction pour rafraîchir les infos utilisateur (si le user change son avatar par exemple)
  const refreshUser = useCallback(async () => {
    try {
      const data = await getMe();
      setUser(data);
    } catch {
      console.error('Erreur lors du rafraîchissement des infos utilisateur');
      logout();
    }
  }, [logout]);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}
