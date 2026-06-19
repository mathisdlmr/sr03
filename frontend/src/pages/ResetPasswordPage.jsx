import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/authApi';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [password, setPassword] = useState('');
  const [password2, setPassword2] = useState('');
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (password !== password2) {
      setError('Les mots de passe ne correspondent pas.');
      return;
    }

    setLoading(true);
    try {
      const data = await resetPassword(token, password);
      setSuccess(data.success);
    } catch (err) {
      setError(err.message || 'Une erreur est survenue...');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-20">
      <div className="row">
        <div className="cell-md-6 offset-md-3">
          <div className="border border-size-1 border-radius-10 bd-blue p-8">
            <div className="text-center mb-6">
              <h2 className="text-bold mt-2">Nouveau mot de passe</h2>
              <p className="text-muted">Choisissez un nouveau mot de passe pour votre compte.</p>
            </div>

            {error && (
              <div className="alert border-radius-2 alert-warning mb-4">
                <span className="mif-warning mx-2" />
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className="success border-radius-2 mb-4">
                <span className="mif-checkmark mx-2" />
                <span>{success}</span>
                <div className="mt-2">
                  <Link to="/login">Se connecter</Link>
                </div>
              </div>
            )}

            {!success && (
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label className="label-for-input">
                    Nouveau mot de passe <span className="fg-red">*</span>
                  </label>
                  <input
                    type="password"
                    data-role="input"
                    placeholder="*********"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    minLength={8}
                  />
                  <small className="text-muted">
                    Min. 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre
                  </small>
                </div>

                <div className="form-group mt-4">
                  <label className="label-for-input">
                    Confirmer le mot de passe <span className="fg-red">*</span>
                  </label>
                  <input
                    type="password"
                    data-role="input"
                    placeholder="*********"
                    value={password2}
                    onChange={e => setPassword2(e.target.value)}
                    required
                    minLength={8}
                  />
                </div>

                <div className="form-group mt-6">
                  <button
                    type="submit"
                    className="button info bg-blue fg-white text-bold w-100"
                    disabled={loading}
                  >
                    {loading && <span className="mif-spinner2 ani-spin mr-2" />}
                    Réinitialiser le mot de passe
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
