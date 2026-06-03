import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate   = useNavigate();

  const [mail, setMail]         = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

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
    <div>
      <div>
        <div>
          <div>
            <div>
              <h2>SR03 Chat</h2>
              <p>Connectez-vous pour accéder à votre espace</p>
            </div>
            {error && (
              <div>
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div>
                <label>Adresse e-mail</label>
                <input
                  id="login-mail"
                  type="email"
                  data-role="input"
                  placeholder="john.doe@mail.fr"
                  value={mail}
                  onChange={(e) => setMail(e.target.value)}
                  required
                />
              </div>

              <div>
                <label>Mot de passe</label>
                <input
                  id="login-password"
                  type="password"
                  data-role="input"
                  placeholder="*********"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <div>
                <button
                  id="login-submit"
                  type="submit"
                  disabled={loading}
                >
                  Se connecter
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
