import { useEffect, useState } from 'react';
import { deleteInvite, getInvitedChats} from '../api/apiCalls';
import { formatDateTime } from '../utils/dateUtils';

export default function InvitationsPage() {
  const [chats, setChats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getInvitedChats()
      .then((data) => setChats(data))
      .catch(() => setError('Impossible de charger vos invitations...'))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (idChat) => {
    if (!confirm(`Supprimer l'invitation (vous n'aurez plus accès au salon) ?`)) return;
    try {
      await deleteInvite(idChat);
      setChats((prev) => prev.filter((c) => c.id !== idChat));
    } catch {
      alert('Erreur lors de la suppression...');
    }
  };

  const openChat = (chatId) => {
    window.open(`/chat/${chatId}`);
  };

  return (
    <div>
      <h1 className="text-bold mb-2">Mes invitations</h1>
      <p className="text-muted mb-6">Salons auxquels vous avez été invité</p>

      {error && (
        <div className="alert alert-warning border-radius-2 mb-4">
          <span className="mif-warning mx-2" />{error}
        </div>
      )}

      {loading ? (
        <div className="d-flex flex-justify-center p-10">
          <span className="mif-spinner2 ani-spin mif-3x fg-blue" />
        </div>
      ) : chats.length === 0 ? (
        <div className="border border-size-1 border-radius-6 p-8 text-center text-muted">
          <span className="mif-envelope mif-4x d-block mb-4" />
          Vous n'avez pas encore reçu d'invitation.
        </div>
      ) : (
        <table className="table border striped">
          <thead>
            <tr>
              <th>Titre</th>
              <th>Description</th>
              <th>Créé le</th>
              <th>Se termine le</th>
              <th className="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {chats.map((chat) => (
              <tr key={chat.id}>
                <td className="text-bold">{chat.title}</td>
                <td className="text-muted">{chat.description || '--'}</td>
                <td>{formatDateTime(chat.createdAt)}</td>
                <td>{formatDateTime(chat.endsAt)}</td>
                <td className="text-right">
                  <button
                    className="button small info mr-2"
                    onClick={() => openChat(chat.id)}
                    title="Rejoindre le chat"
                  >
                    <span className="mif-chat mr-1" /> Rejoindre
                  </button>
                  <button
                      className="button small alert"
                      onClick={() => handleDelete(chat.id)}
                      title="Supprimer l'invitation"
                  >
                    <span className="mif-bin" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
