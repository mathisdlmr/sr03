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
    <div className="row">
      <div className="cell-md-8 offset-md-2">
        <h1 className="text-bold mb-2">Planifier une discussion</h1>
        <p className="text-muted mb-6">Créez un nouveau salon de discussion.</p>

        <div className="border border-size-1 border-radius-6 p-8 shadow-normal">
          {error && (
            <div className="alert alert-warning border-radius-2 mb-4">
              <span className="mif-warning mx-2" />
              {error}
            </div>
          )}
          {success && (
            <div className="alert alert-success border-radius-2 mb-4">
              <span className="mif-checkmark mx-2" />
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="label-for-input">Titre du salon *</label>
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

            <div className="form-group mt-4">
              <label className="label-for-input">Description</label>
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

            <div className="form-group mt-6 d-flex flex-justify-between">
              <button
                type="button"
                className="button secondary"
                onClick={() => navigate('/salons')}
              >
                Annuler
              </button>
              <button
                id="create-chat-submit"
                type="submit"
                className="button info bg-blue fg-white"
                disabled={loading}
              >
                {loading && <span className="mif-spinner2 ani-spin mr-2" />}
                Créer le salon
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
