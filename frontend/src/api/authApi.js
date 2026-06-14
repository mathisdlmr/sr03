import axios from 'axios';
import { apiGet } from './apiClient';

const API_URL = '/api';

/**
 * Le fichier authApi est responsable de la communication avec le service d'authentification de l'API : "/api/auth/..."
 * On utilise ce fichier pour définir les fonctions de login, mot de passe oublié, et réinitialisation de mot de passe
 * Ce fichier ne se base pas sur apiClient car l'apiClient suppose que l'utilisateur est déjà authentifié
 * Les requêtes ici formulées n'ont donc pas d'access token en header
 */

// POST /api/auth/login - Authentification de l'utilisateur
// Body : mail: string, password: string
export const login = async (mail, password) => {
  const res = await axios.post(`${API_URL}/auth/login`, new URLSearchParams({ mail, password }));
  return res.data;
};

// POST /api/auth/forgot-password - Demander un email de réinitialisation de mot de passe
// Body : mail: string
export const forgotPassword = async mail => {
  const res = await axios.post(`${API_URL}/auth/forgot-password`, new URLSearchParams({ mail }));
  return res.data;
};

// POST /api/auth/reset-password - Réinitialiser le mot de passe
// Body : token: string, password: string
export const resetPassword = async (token, password) => {
  const res = await axios.post(
    `${API_URL}/auth/reset-password`,
    new URLSearchParams({ token, password })
  );
  return res.data;
};
