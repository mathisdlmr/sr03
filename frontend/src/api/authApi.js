import axios from 'axios';
import { apiGet, apiPost } from './apiClient';

const API_URL = '/api';

/**
 * POST /api/auth/login
 * Body (form-data ou JSON) : mail, password
 * Retourne : access_token (24h), refresh_token (7j), info de l'utilisateur connecté
*/
export const login = async (mail, password) => {
  const res = await axios.post(
    `${API_URL}/auth/login`,
    new URLSearchParams({ mail, password })
  );
  return res.data;
};

/**
 * POST /api/auth/refresh
 * Header : Authorization: Bearer <refresh_token>
 * Retourne : nouvel access_token + nouvel refresh_token
*/
export const refresh = async (refreshToken) => {
  const res = await axios.post(`${API_URL}/auth/refresh`, null, {
    headers: { Authorization: `Bearer ${refreshToken}` },
  });
  return res.data;
};

/**
 * GET /api/auth/me
 * Retourne : les infos de l'utilisateur connecté (extrait du JWT)
*/
export const getMe = () => apiGet('auth/me');
