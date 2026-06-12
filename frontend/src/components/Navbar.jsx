import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  function MouseOver(event) {
    event.target.style.borderRadius = '100%';
    event.target.style.border = '2px solid #0000FF';
  }
  function MouseOut(event){
    event.target.style.border='2px solid #FFFFFF';
  }

  return (
    <header className="border border-size-1 bd-gray shadow-normal" data-role="appbar" data-expand-point="md">
      <ul className="app-bar-menu" style={{ fontSize: '14px' }}>
        <li>
          <NavLink to="/home" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            <span className="mif-home mr-1" /> Accueil
          </NavLink>
        </li>
        <li>
          <NavLink to="/planifier" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            <span className="mif-plus mr-1" /> Planifier une discussion
          </NavLink>
        </li>
        <li>
          <NavLink to="/salons" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            <span className="mif-chat mr-1" /> Mes salons
          </NavLink>
        </li>
        <li>
          <NavLink to="/invitations" className={({ isActive }) => isActive ? 'text-bold' : ''}>
            <span className="mif-mail mr-1" /> Mes invitations
          </NavLink>
        </li>
      </ul>

      <div className="app-bar-item-static mx-auto d-flex flex-align-center">
        {user && (
          <span className="h3 mt-3">{user.firstname} {user.lastname}</span>
        )}
      </div>

      <div className="app-bar-item-static ml-auto">
        {user && (
          <NavLink to="/profil" className="d-flex flex-align-center" style={{ textDecoration: 'none', color: 'inherit' }} onMouseOver={MouseOver} onMouseOut={MouseOut}>
            {user.avatar ? (
              <img
                src={user.avatar}
                alt="avatar"
                style={{ width: 32, height: 32, borderRadius: '50%', objectFit: 'cover', marginRight: 8 }}
              />
            ) : (
              <span className="mif-account-circle mif-4x fg-blue mr-2" />
            )}
          </NavLink>
        )}
        <button className="button small alert ml-1" onClick={handleLogout}>
          <span className="mif-switch mr-1" /> Déconnexion
        </button>
      </div>
    </header>
  );
}
