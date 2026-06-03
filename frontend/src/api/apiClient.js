import axios from 'axios';

const API_URL = '/api';

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
  } catch (e) {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    throw new Error('JWT expired');
  }
};

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
