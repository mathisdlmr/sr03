import { useState, useRef } from 'react';
import { useAuth } from '../hooks/useAuth';
import { uploadAvatar, deleteAvatar } from '../api/apiCalls';

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const fileInputRef = useRef(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleAvatarUpload = async e => {
    const file = e.target.files?.[0];
    if (!file) {
      setError('Aucun fichier sélectionné.');
      return;
    }

    if (!file.type.startsWith('image/')) {
      setError('Seules les images sont acceptées.');
      return;
    }
    if (file.size > 1_048_576) {
      setError("L'image ne doit pas dépasser 1 Mo.");
      return;
    }

    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await uploadAvatar(file);
      setSuccess('Avatar mis à jour !');
      if (refreshUser) {
        await refreshUser();
      }
    } catch (err) {
      setError(err.message || "Erreur lors de l'upload de l'avatar.");
    } finally {
      setLoading(false);
      e.target.value = '';
    }
  };

  const handleDeleteAvatar = async () => {
    if (!confirm('Supprimer votre avatar ?')) {
      return;
    }
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await deleteAvatar();
      setSuccess('Avatar supprimé.');
      if (refreshUser) {
        await refreshUser();
      }
    } catch (err) {
      setError(err.message || "Erreur lors de la suppression de l'avatar.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="row">
      <div className="cell-md-6 offset-md-3">
        <h1 className="text-bold mb-2">Mon profil</h1>
        <p className="text-muted mb-6">Gérez vos informations personnelles et votre avatar.</p>

        <div className="border border-size-1 border-radius-6 p-8 shadow-normal">
          {error && (
            <div className="alert alert-warning border-radius-2 mb-4">
              <span className="mif-warning mx-2" />
              {error}
            </div>
          )}
          {success && (
            <div className="success border-radius-2 mb-4">
              <span className="mif-checkmark mx-2" />
              {success}
            </div>
          )}

          <div className="d-flex flex-align-center mb-6">
            <div style={{ marginRight: 20 }}>
              {user?.avatar ? (
                <img
                  src={user.avatar}
                  alt="avatar"
                  style={{
                    width: 100,
                    height: 100,
                    borderRadius: '50%',
                    objectFit: 'cover',
                    border: '3px solid #4a90d9',
                  }}
                />
              ) : (
                <div
                  style={{
                    width: 100,
                    height: 100,
                    borderRadius: '50%',
                    backgroundColor: '#4a90d9',
                    color: '#fff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 36,
                    fontWeight: 'bold',
                  }}
                >
                  {user?.firstname?.[0]}
                  {user?.lastname?.[0]}
                </div>
              )}
            </div>
            <div>
              <h3 className="mb-1">
                {user?.firstname} {user?.lastname}
              </h3>
              <p className="text-muted mb-0">{user?.mail}</p>
              <span
                className={`badge inline mt-2 ${user?.admin ? 'bg-blue fg-white' : 'bg-gray fg-white'}`}
                style={{ fontSize: '0.9rem' }}
              >
                {user?.admin ? 'Administrateur' : 'Utilisateur'}
              </span>
            </div>
          </div>

          <h4 className="mb-4">
            <span className="mif-images mr-2" />
            Avatar
          </h4>
          <p className="text-muted mb-4">
            Choisissez une image (max 1 Mo, format carré recommandé). L&apos;avatar sera affiché
            dans le chat et la barre de navigation.
          </p>

          <input
            type="file"
            ref={fileInputRef}
            style={{ display: 'none' }}
            accept=".jpg,.png"
            onChange={handleAvatarUpload}
          />

          <div className="d-flex gap-2">
            <button
              className="button info"
              onClick={() => fileInputRef.current?.click()}
              disabled={loading}
            >
              {loading ? (
                <span className="mif-spinner2 ani-spin mr-2" />
              ) : (
                <span className="mif-upload mr-2" />
              )}
              Changer l&apos;avatar
            </button>
            {user?.avatar && (
              <button className="button alert" onClick={handleDeleteAvatar} disabled={loading}>
                <span className="mif-bin mr-2" />
                Supprimer
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
