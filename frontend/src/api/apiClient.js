import axios from 'axios';

/**
 * Le fichier apiClient est responsable de la communication avec l'API
 * Il permet de normaliser les requêtes (GET, POST, DELETE) formulées au backend
 * En particulier, il gère l'ajout du token d'accès dans les headers, et le rafraîchissement des tokens en cas d'expiration
 */

const API_URL = '/api';

// Fonction pour rafraîchir les tokens d'accès : envoie une requête POST à /api/auth/refresh avec le refresh token dans les headers
// Si le rafraîchissement réussit, les nouveaux tokens sont stockés dans localStorage
const refreshTokens = async () => {
  const refreshToken = localStorage.getItem('refresh_token');
  if (!refreshToken) {
    return null;
  }

  try {
    const res = await axios.post(`${API_URL}/auth/refresh`, null, {
      headers: { Authorization: `Bearer ${refreshToken}` },
    });
    localStorage.setItem('access_token', res.data.access_token);
    localStorage.setItem('refresh_token', res.data.refresh_token);
    return true;
  } catch (error) {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    throw new Error(error.response?.data?.error || 'JWT expired');
  }
};

// Fonction pour envoyer une requête GET à l'API (avec l'access token en header)
export const apiGet = async (url) => {
  const accessToken = localStorage.getItem('access_token');
  const config = {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  };

  try {
    const response = await axios.get(`${API_URL}/${url}`, config);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      console.error('JWT expired, attempting to refresh tokens...');
      await refreshTokens();
      return await apiGet(url); // Retry avec le nouveau token
    } else {
      throw new Error(error.response?.data?.error || 'RequestError');
    }
  }
};

// Fonction pour envoyer une requête POST à l'API (avec l'access token en header)
// Le paramètre options permet de spécifier un content type différent (ex: application/json) si nécessaire
export const apiPost = async (url, data, options = {}) => {
  const accessToken = localStorage.getItem('access_token');
  const headers = accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

  if (options.contentType) {
    headers['Content-Type'] = options.contentType;
  }

  try {
    const response = await axios.post(`${API_URL}/${url}`, data, { headers });
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      console.error('JWT expired, attempting to refresh tokens...');
      await refreshTokens();
      return await apiPost(url, data, options); // Retry avec le nouveau token
    } else {
      throw new Error(error.response?.data?.error || 'RequestError');
    }
  }
};

// Fonction pour envoyer une requête DELETE à l'API (avec l'access token en header)
export const apiDelete = async (url) => {
  const accessToken = localStorage.getItem('access_token');
  const config = {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  };

  try {
    const response = await axios.delete(`${API_URL}/${url}`, config);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      console.error('JWT expired, attempting to refresh tokens...');
      await refreshTokens();
      return await apiDelete(url); // Retry avec le nouveau token
    } else {
      throw new Error(error.response?.data?.error || 'RequestError');
    }
  }
};
