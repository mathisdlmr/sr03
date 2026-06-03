import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createChat } from '../api/chatApi';

export default function PlanifierPage() {
  const navigate = useNavigate();

  const [title, setTitle]           = useState('');
  const [description, setDescription] = useState('');
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');
  const [loading, setLoading]       = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Le titre est obligatoire...');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await createChat({ title: title.trim(), description: description.trim() });
      setSuccess('Salon créé avec succès !');
      setTitle('');
      setDescription('');
      setTimeout(() => navigate('/salons'), 1500);
    } catch {
      setError('Erreur lors de la création du salon... Veuillez réessayer.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div>
        <h1>Planifier une discussion</h1>
        <p>Créez un nouveau salon de discussion.</p>

        <div>
          {error && (
            <div>
              {error}
            </div>
          )}
          {success && (
            <div>
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div>
              <label>Titre du salon *</label>
              <input
                id="chat-title"
                type="text"
                data-role="input"
                placeholder="ex : Réunion projet SR03"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div>
              <label>Description</label>
              <textarea
                id="chat-description"
                data-role="textarea"
                placeholder="Décrivez l'objet de cette discussion..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={4}
                style={{ width: '100%', resize: 'vertical' }}
              />
            </div>

            <div>
              <button
                type="button"
                onClick={() => navigate('/salons')}
              >
                Annuler
              </button>
              <button
                id="create-chat-submit"
                type="submit"
                disabled={loading}
              >
                {loading && <span />}
                Créer le salon
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
