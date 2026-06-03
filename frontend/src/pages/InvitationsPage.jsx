import { useEffect, useState } from 'react';
import { getInvitedChats } from '../api/chatApi';
import { formatDateTime } from '../utils/dateUtils';

export default function InvitationsPage() {
  const [chats, setChats]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    getInvitedChats().then((data) => setChats(data)).catch(() => setError('Impossible de charger vos invitations...')).finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1>Mes invitations</h1>
      <p>Salons auxquels vous avez été invité</p>

      {error && (
        <div>
          {error}
        </div>
      )}

      {loading ? (
        <div>
          Loading
        </div>
      ) : chats.length === 0 ? (
        <div>
          Vous n'avez pas encore reçu d'invitation.
        </div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Titre</th>
              <th>Description</th>
              <th>Créé le</th>
              <th>Se termine le</th>
            </tr>
          </thead>
          <tbody>
            {chats.map((chat) => (
              <tr key={chat.id}>
                <td>{chat.title}</td>
                <td>{chat.description || '--'}</td>
                <td>{formatDateTime(chat.createdAt)}</td>
                <td>{formatDateTime(chat.endsAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
