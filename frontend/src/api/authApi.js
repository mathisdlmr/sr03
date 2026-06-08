import axios from 'axios';
import { apiGet } from './apiClient';

const API_URL = '/api';

/**
 * Le fichier authApi est responsable de la communication avec le service d'authentification de l'API : "/api/auth/..."
 * On utilise ce fichier pour définir les fonctions de login, refresh des token, mot de passe oublié, etc.
 * Ce fichier ne se base pas sur apiClient car l'apiClient suppose que l'utilisateur est déjà authentifié
 */

export const login = async (mail, password) => {
  const res = await axios.post(
    `${API_URL}/auth/login`,
    new URLSearchParams({ mail, password })
  );
  return res.data;
};

export const refresh = async (refreshToken) => {
  const res = await axios.post(`${API_URL}/auth/refresh`, null, {
    headers: { Authorization: `Bearer ${refreshToken}` },
  });
  return res.data;
};

export const getMe = () => apiGet('auth/me');

export const forgotPassword = async (mail) => {
  const res = await axios.post(
    `${API_URL}/auth/forgot-password`,
    new URLSearchParams({ mail })
  );
  return res.data;
};

export const resetPassword = async (token, password) => {
  const res = await axios.post(
    `${API_URL}/auth/reset-password`,
    new URLSearchParams({ token, password })
  );
  return res.data;
};
