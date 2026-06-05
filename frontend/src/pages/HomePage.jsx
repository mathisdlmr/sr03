import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getMyChats, getInvitedChats } from '../api/chatApi';

export default function HomePage() {
  const { user } = useAuth();
  const [ownedCount, setOwnedCount]     = useState('...');
  const [invitedCount, setInvitedCount] = useState('...');

  useEffect(() => {
    getMyChats().then((data) => setOwnedCount(data.length)).catch(() => setOwnedCount('?'));
    getInvitedChats().then((data) => setInvitedCount(data.length)).catch(() => setInvitedCount('?'));
  }, []);

  return (
    <div>
      <h1 className="text-bold mb-2">
        Bonjour, {user?.firstname} {user?.lastname} !
      </h1>
      <p className="text-muted mb-6">Bienvenue le Chat de SR03</p>

      <div className="row">
        <div className="cell-md-4">
          <div className="border border-size-1 border-radius-6 p-6 bd-blue shadow-normal">
            <div className="d-flex flex-align-center mb-4">
              <span className="mif-chat mif-3x fg-blue mr-4" />
              <div>
                <div className="text-leader2 text-bold fg-blue">{ownedCount}</div>
                <div className="text-muted">Salon{ownedCount !== 1 ? 's' : ''} créé{ownedCount !== 1 ? 's' : ''}</div>
              </div>
            </div>
            <Link to="/salons" className="button info small w-100">
              Voir mes salons
            </Link>
          </div>
        </div>

        <div className="cell-md-4">
          <div className="border border-size-1 border-radius-6 p-6 bd-blue shadow-normal">
            <div className="d-flex flex-align-center mb-4">
              <span className="mif-envelope mif-3x fg-blue mr-4" />
              <div>
                <div className="text-leader2 text-bold fg-blue">{invitedCount}</div>
                <div className="text-muted">Invitation{invitedCount !== 1 ? 's' : ''} reçue{invitedCount !== 1 ? 's' : ''}</div>
              </div>
            </div>
            <Link to="/invitations" className="button info small w-100">
              Voir mes invitations
            </Link>
          </div>
        </div>

        <div className="cell-md-4">
          <div className="border border-size-1 border-radius-6 p-6 bd-blue shadow-normal">
            <div className="d-flex flex-align-center mb-4">
              <span className="mif-plus mif-3x fg-blue mr-4" />
              <div>
                <div className="text-leader2 text-bold fg-blue">Nouveau</div>
                <div className="text-muted">Planifier une discussion</div>
              </div>
            </div>
            <Link to="/planifier" className="button info small w-100">
              Créer un salon
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
