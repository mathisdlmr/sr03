import { apiGet, apiPost, apiDelete } from './apiClient';

// GET /api/chats/mine - Salons créés par l'utilisateur connecté
export const getMyChats = () => apiGet('chats/mine');

// GET /api/chats/invited - Salons auxquels l'utilisateur est invité
export const getInvitedChats = () => apiGet('chats/invited');

// POST /api/chats - Créer un salon
// Body (JSON) : title: string, description: string
export const createChat = (data) =>
  apiPost('chats', data, { contentType: 'application/json' });

// DELETE /api/chats/:id - Supprimer un salon
export const deleteChat = (id) => apiDelete(`chats/${id}`);

// POST /api/invitations - Inviter un utilisateur dans un salon
// Body (JSON) : idUser: number, idChat: number
export const inviteUser = (idUser, idChat) => apiPost('invitations', new URLSearchParams({ idUser, idChat }));
