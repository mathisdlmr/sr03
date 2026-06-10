import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useParams } from "react-router";
import { useAuth } from '../hooks/useAuth';

import AppLayout      from '../layouts/AppLayout';
import LoginPage      from '../pages/LoginPage';
import HomePage       from '../pages/HomePage';
import PlanifierPage  from '../pages/PlanifierPage';
import SalonsPage     from '../pages/SalonsPage';
import InvitationsPage from '../pages/InvitationsPage';
import ChatPageWebsocket from '../pages/ChatPage';

// Redirige vers /login si non authentifié.
// Affiche un spinner le temps que AuthContext vérifie le token stocké.
function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="d-flex flex-justify-center flex-align-center" style={{ height: '100vh' }}>
        <span className="mif-spinner2 ani-spin mif-4x fg-blue" />
      </div>
    );
  }

  return user ? children : <Navigate to="/login" replace />;
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Route de login constamment accessible */ }
        <Route path="/login" element={<LoginPage />} /> 

        {/* Routes protégées, accessibles seulement si authentifié */ }
        <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route index element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/planifier" element={<PlanifierPage />} />
          <Route path="/salons" element={<SalonsPage />} />
          <Route path="/invitations" element={<InvitationsPage />} />
          <Route path="/chat/:chatId" element={<ChatPageWebsocket />} />
        </Route>

        {/* Fallback vers /home pour toute route inconnue */ }
        <Route path="*" element={<Navigate to="/home" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
