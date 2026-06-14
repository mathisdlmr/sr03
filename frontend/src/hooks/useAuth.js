import { useContext } from 'react';
import { AuthContext } from '../contexts/authContext';

// Hook pour accéder au contexte d'authentification
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé dans un <AuthProvider>');
  }
  return context;
}
