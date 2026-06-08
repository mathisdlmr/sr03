import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyChats, deleteChat } from '../api/chatApi';
import { formatDateTime } from '../utils/dateUtils';

export default function SalonsPage() {
  const [chats, setChats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadChats = () => {
    setLoading(true);
    getMyChats()
      .then((data) => setChats(data))
      .catch(() => setError('Impossible de charger vos salons...'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadChats();
  }, []);

  const handleDelete = async (id) => {
    if (!confirm('Supprimer ce salon ?')) return;
    try {
      await deleteChat(id);
      setChats((prev) => prev.filter((c) => c.id !== id));
    } catch {
      alert('Erreur lors de la suppression...');
    }
  };

  const openChat = (chatId) => {
    window.open(`/chat/${chatId}`);
  };

  return (
    <div>
      <div className="d-flex flex-justify-between flex-align-center mb-6">
        <div>
          <h1 className="text-bold mb-1">Mes salons de discussion</h1>
          <p className="text-muted">Salons dont vous êtes le créateur</p>
        </div>
        <Link to="/planifier" className="button info">
          <span className="mif-plus mr-2" />
          Nouveau salon
        </Link>
      </div>

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
          <span className="mif-chat mif-4x d-block mb-4" />
          Vous n'avez pas encore créé de salon.&nbsp;
          <Link to="/planifier">Créer votre premier salon</Link>
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
                <td className="text-italic">{chat.description || '--'}</td>
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
                    title="Supprimer"
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
