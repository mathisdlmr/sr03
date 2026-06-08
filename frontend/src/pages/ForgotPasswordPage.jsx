import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../api/authApi';

export default function ForgotPasswordPage() {
  const [mail, setMail] = useState('');
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const data = await forgotPassword(mail);
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
              <h2 className="text-bold mt-2">Mot de passe oublié</h2>
              <p className="text-muted">Entrez votre adresse email pour recevoir un lien de réinitialisation.</p>
            </div>

            {error && (
              <div className="alert border-radius-2 alert-warning mb-4">
                <span className="mif-warning mx-2" />
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className="alert border-radius-2 alert-success mb-4">
                <span className="mif-mail mx-2" />
                <span>{success}</span>
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="label-for-input">Adresse e-mail</label>
                <input
                  type="email"
                  data-role="input"
                  data-prepend="&lt;span class='mif-person'&gt;"
                  placeholder="john.doe@mail.fr"
                  value={mail}
                  onChange={(e) => setMail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group mt-6">
                <button
                  type="submit"
                  className="button info bg-blue fg-white text-bold w-100"
                  disabled={loading}
                >
                  {loading && <span className="mif-spinner2 ani-spin mr-2" />}
                  Envoyer le lien de réinitialisation
                </button>
              </div>
            </form>

            <div className="text-center mt-4">
              <Link to="/login">Retour à la connexion</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
