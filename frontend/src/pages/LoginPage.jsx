import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [mail, setMail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(mail, password);
      navigate('/home');
    } catch (err) {
      const msg = err.response?.data?.error || 'Identifiants incorrects...';
      setError(msg);
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
              <h2 className="text-bold mt-2">SR03 Chat</h2>
              <p className="text-muted">Connectez-vous pour accéder à votre espace</p>
            </div>

            {error && (
              <div className="alert border-radius-2 alert-warning mb-4">
                <span className="mif-warning mx-2" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="label-for-input">Adresse e-mail</label>
                <input
                  id="login-mail"
                  type="email"
                  data-role="input"
                  data-prepend="&lt;span class='mif-person'&gt;"
                  placeholder="john.doe@mail.fr"
                  value={mail}
                  onChange={(e) => setMail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group mt-4">
                <label className="label-for-input">Mot de passe</label>
                <input
                  id="login-password"
                  type="password"
                  data-role="input"
                  data-prepend="<span class='mif-lock'></span>" data-reveal-button-icon="<span class='mif-eye mif-2x'></span>"
                  placeholder="*********"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <div className="form-group mt-6">
                <button
                  id="login-submit"
                  type="submit"
                  className="button info bg-blue fg-white text-bold w-100"
                  disabled={loading}
                >
                  {loading
                    ? <span className="mif-spinner2 ani-spin mr-2" />
                    : <span className="mif-enter mr-2" />}
                  Se connecter
                </button>
              </div>

              <div className="text-center mt-4">
                <Link to="/forgot-password">Mot de passe oublié ?</Link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
