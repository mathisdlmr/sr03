import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="border border-size-1 bd-gray shadow-normal" data-role="appbar" data-expand-point="md">
      <ul className="app-bar-menu">
        <li>
          <NavLink to="/home" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            Accueil
          </NavLink>
        </li>
        <li>
          <NavLink to="/planifier" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            Planifier une discussion
          </NavLink>
        </li>
        <li>
          <NavLink to="/salons" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            Mes salons de discussion
          </NavLink>
        </li>
        <li>
          <NavLink to="/invitations" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            Mes invitations
          </NavLink>
        </li>
      </ul>

      <div className="app-bar-item-static mx-auto">
        {user && (
          <span className="text-muted">
            {user.firstname} {user.lastname}
          </span>
        )}
      </div>

      <div className="app-bar-item-static ml-auto">
        <button className="button small alert ml-1" onClick={handleLogout}>
          Déconnexion
        </button>
      </div>
    </header>
  );
}
