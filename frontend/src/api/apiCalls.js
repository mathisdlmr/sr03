import { apiGet, apiPost, apiDelete } from './apiClient';
import axios from 'axios';

const API_URL = '/api';

/**
 * Le fichier apiCalls se base sur le fichier apiClient pour réaliser des requêtes au backend
 * Ce fichier permet donc simplement de définir des fonctions à partir de endpoints spécifiques
 * Ce n'est donc qu'un condensé des endpoints que l'on retrouve dans `src/main/java/fr/utc/sr03/controller/ApiController.java`
 */

// GET /api/auth/me - Récupérer les informations de l'utilisateur connecté
export const getMe = () => apiGet('auth/me');

// GET /api/chats/mine - Salons créés par l'utilisateur connecté
export const getMyChats = () => apiGet('chats/mine');

// GET /api/chats/invited - Salons auxquels l'utilisateur est invité
export const getInvitedChats = () => apiGet('chats/invited');

// POST /api/chats - Créer un salon
// Body (JSON) : title: string, description: string, startsAt: string, durationMinutes: number
export const createChat = (data) => apiPost('chats', data, { contentType: 'application/json' });

// DELETE /api/chats/:id - Supprimer un salon
export const deleteChat = (id) => apiDelete(`chats/${id}`);

// POST /api/invitations - Inviter un utilisateur dans un salon
// Body (JSON) : idUser: number, idChat: number
export const inviteUser = (idUser, idChat) => apiPost('invitations', new URLSearchParams({ idUser, idChat }));

// GET /api/users/search?q=... - Recherche d'utilisateurs (pour l'autocomplétion)
export const searchUsers = (q) => apiGet(`users/search?q=${encodeURIComponent(q)}`);

// POST /api/auth/avatar - Upload d'avatar
export const uploadAvatar = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const accessToken = localStorage.getItem('access_token');
  const res = await axios.post(`${API_URL}/auth/avatar`, formData, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'multipart/form-data',
    },
  });
  return res.data;
};

// DELETE /api/auth/avatar - Supprimer l'avatar
export const deleteAvatar = () => apiDelete('auth/avatar');
