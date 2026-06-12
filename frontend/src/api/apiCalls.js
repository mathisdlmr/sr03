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

// GET /api/chats/:id - Informations du salon
export const getChat = (id) => apiGet(`chats/${id}`)

// DELETE /api/chats/:id - Supprimer un salon
export const deleteChat = (id) => apiDelete(`chats/${id}`);

// GET /api/invitations/{chatId} - Récupère la liste des invitations invited au chat
export const getInvitationsToChat = (idChat) => apiGet(`invitations/${idChat}`);

// GET /api/invitations/{chatId} - Récupère la liste des utilisateurices invited au chat
export const getInvitedUsersToChat = (idChat) => apiGet(`invitations/users/${idChat}`);

// POST /api/invitations - Inviter un utilisateur dans un salon
// Body (JSON) : idUser: number, idChat: number
export const inviteUser = (idUser, idChat) => apiPost('invitations', new URLSearchParams({ idUser, idChat }));

// DELETE /api/invitations/{chatId} - Supprime l'invitation à un chat de l'utilisateur connecté
export const deleteInvite = (idChat) => apiDelete(`invitations/${idChat}`);

// DELETE /api/invitations/{userId}/{chatId} - Supprime l'invitation d'un utilisateur à un chat
export const deleteInviteUser = (idUser, idChat) => apiDelete(`invitations/${idUser}/${idChat}`);

// GET /api/users/{chatId}/search?q=... - Recherche d'utilisateurs (pour l'autocomplétion)
export const searchUninvitedUsers = (idChat, q) => apiGet(`users/${idChat}/search?q=${encodeURIComponent(q)}`);

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
