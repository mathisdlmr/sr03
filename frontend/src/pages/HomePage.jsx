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
      <h1>
        Bonjour, {user?.firstname} {user?.lastname} !
      </h1>
      <p>Bienvenue le Chat de SR03</p>

      <div>
        <div>
          <div>
            <div>{ownedCount}</div>
            <div>Salon{ownedCount !== 1 ? 's' : ''} créé{ownedCount !== 1 ? 's' : ''}</div>
          </div>
          <Link to="/salons">
            Voir mes salons
          </Link>
        </div>

        <div>
          <div>
            <div>{invitedCount}</div>
            <div>Invitation{invitedCount !== 1 ? 's' : ''} reçue{invitedCount !== 1 ? 's' : ''}</div>
          </div>
          <Link to="/invitations">
            Voir mes invitations
          </Link>
        </div>

        <div>
          <div>
            <div>Nouveau</div>
            <div>Planifier une discussion</div>
          </div>
          <Link to="/planifier">
            Créer un salon
          </Link>
        </div>
      </div>
    </div>
  );
}
