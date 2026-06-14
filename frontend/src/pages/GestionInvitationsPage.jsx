import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  deleteInviteUser,
  getChat,
  getInvitedUsersToChat,
  inviteUser,
  searchUninvitedUsers,
} from '../api/apiCalls';

export default function SalonsPage() {
  const { chatId } = useParams();

  const [loading, setLoading] = useState(() => Boolean(chatId));
  const [chat, setChat] = useState([]);
  const [listUsers, setUsers] = useState([]);
  const [listInvitedUsers, setInvitedUsers] = useState([]);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(() => (chatId ? '' : 'Chat non trouvé.'));

  const loadInvitedUsers = chatId =>
    getInvitedUsersToChat(chatId)
      .then(data => setInvitedUsers(data))
      .catch(() => setError('Impossible de charger les utilisateurices...'));

  useEffect(() => {
    if (!chatId) {
      return;
    }

    // On récupère le chat
    const chatPromise = getChat(chatId)
      .then(data => setChat(data))
      .catch(() => setError('Impossible de charger le chat...'));

    Promise.all([chatPromise, loadInvitedUsers(chatId)]).finally(() => setLoading(false));
  }, [chatId]);

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setLoading(true);

    searchUninvitedUsers(chatId, search)
      .then(data => setUsers(data))
      .catch(() => setError('Impossible de chercher les utilisateuricess invites...'))
      .finally(() => setLoading(false));
  };

  const toggleInvite = async userId => {
    if (!confirm(`Inviter l'utilisateurice au salon ?`)) return;
    try {
      await inviteUser(userId, chatId);
      loadInvitedUsers(chatId);
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch {
      alert(`Erreur lors de l'ajout...`);
    }
  };

  const resetSearch = () => {
    setSearch('');
  };

  const handleDeleteInvite = async (userId, chatId) => {
    if (!confirm(`Retirer l'utilisateurice du chat ?`)) return;
    try {
      await deleteInviteUser(userId, chatId);
      loadInvitedUsers(chatId);
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch {
      alert('Erreur lors de la suppression...');
    }
  };

  return (
    <div>
      <main className="container mt-8">
        <h2>Gérer les invitations du salon &quot;{chat.title}&quot; </h2>
        <form onSubmit={handleSubmit} className="row my-4">
          <div className="cell-md-8">
            <input
              type="text"
              className="w-100"
              name="search"
              value={search}
              onChange={s => setSearch(s.target.value)}
              placeholder="Rechercher des utilisateurices à ajouter... (minimum 2 caractères)"
            />
          </div>
          <div className="cell-md-2">
            <button
              type="submit"
              className="button info bg-blue fg-white w-100"
              title="Rechercher des utilisateurices à ajouter"
            >
              Rechercher
            </button>
          </div>
          <div className="cell-md-2">
            <button className="button w-100" onClick={() => resetSearch()} title="Réinitialiser">
              Réinitialiser
            </button>
          </div>
        </form>

        {error && (
          <div className="alert alert-warning border-radius-2 mb-4">
            <span className="mif-warning mx-2" />
            {error}
          </div>
        )}

        {loading ? (
          <div className="d-flex flex-justify-center p-10">
            <span className="mif-spinner2 ani-spin mif-3x fg-blue" />
          </div>
        ) : listUsers?.length === 0 ? (
          <div className="border border-size-1 border-radius-6 p-8 text-center text-muted mt-4">
            <span className="mif-users mif-4x d-block mb-4"></span>
            <p className="text-leader">Ajouter des utilisateurices</p>
          </div>
        ) : (
          <div className="row mt-6">
            <div style={{ overflowX: 'auto', width: '100%' }}>
              <table className="table striped border row-hover row-border">
                <thead>
                  <tr>
                    <th>Nom</th>
                    <th>Prénom</th>
                    <th>Email</th>
                    <th style={{ width: '220px' }} className="text-right">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {listUsers.map(user => (
                    <tr key={user.id}>
                      <td>{user.lastname}</td>
                      <td>{user.firstname}</td>
                      <td>{user.mail}</td>
                      <td className="text-right">
                        <button
                          className="button small info mr-2"
                          onClick={() => toggleInvite(user.id)}
                          title="Inviter au chat"
                        >
                          <span className="mif-plus mr-1" /> Inviter
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <h2>Invité.es du salon : </h2>

        {listInvitedUsers?.length !== 0 && (
          <div>
            <div className="row mt-6">
              <div style={{ overflowX: 'auto', width: '100%' }}>
                <table className="table striped border row-hover row-border">
                  <thead>
                    <tr>
                      <th>Nom</th>
                      <th>Prénom</th>
                      <th>Email</th>
                      <th style={{ width: '220px' }} className="text-right">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {listInvitedUsers.map(user => (
                      <tr key={user.id}>
                        <td>{user.lastname}</td>
                        <td>{user.firstname}</td>
                        <td>{user.mail}</td>
                        <td className="text-right">
                          <button
                            className="button small alert mr-2"
                            onClick={() => handleDeleteInvite(user.id, chat.id)}
                            title="Retirer du chat"
                          >
                            <span className="mif-cross mr-1" /> Retirer
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
