import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyChats, deleteChat } from '../api/chatApi';
import { formatDateTime } from '../utils/dateUtils';

export default function SalonsPage() {
  const [chats, setChats]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  const loadChats = () => {
    setLoading(true);
    getMyChats().then((data) => setChats(data)).catch(() => setError('Impossible de charger vos salons...')).finally(() => setLoading(false));
  };

  useEffect(() => { loadChats(); }, []);

  const handleDelete = async (id) => {
    if (!confirm('Supprimer ce salon ?')) return;
    try {
      await deleteChat(id);
      setChats((prev) => prev.filter((c) => c.id !== id));
    } catch {
      alert('Erreur lors de la suppression...');
    }
  };

  return (
    <div>
      <div>
        <div>
          <h1>Mes salons de discussion</h1>
          <p>Salons dont vous êtes le créateur</p>
        </div>
        <Link to="/planifier">
          <span />
          Nouveau salon
        </Link>
      </div>

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
          Vous n'avez pas encore créé de salon.&nbsp;
          <Link to="/planifier">Créer votre premier salon</Link>
        </div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Titre</th>
              <th>Description</th>
              <th>Créé le</th>
              <th>Se termine le</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {chats.map((chat) => (
              <tr key={chat.id}>
                <td>{chat.title}</td>
                <td>{chat.description || '--'}</td>
                <td>{formatDateTime(chat.createdAt)}</td>
                <td>{formatDateTime(chat.endsAt)}</td>
                <td>
                  <button
                   
                    onClick={() => handleDelete(chat.id)}
                    title="Supprimer"
                  >
                    <span />
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
