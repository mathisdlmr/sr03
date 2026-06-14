import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

import AppLayout from '../layouts/AppLayout';
import LoginPage from '../pages/LoginPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import HomePage from '../pages/HomePage';
import PlanifierPage from '../pages/PlanifierPage';
import SalonsPage from '../pages/SalonsPage';
import InvitationsPage from '../pages/InvitationsPage';
import ChatPage from '../pages/ChatPage';
import ProfilePage from '../pages/ProfilePage';
import GestionInvitationsPage from '../pages/GestionInvitationsPage';

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
        {/* Routes publiques */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Route du chat (ouverte dans une nouvelle fenêtre sans navbar) */}
        <Route
          path="/chat/:chatId"
          element={
            <ProtectedRoute>
              <ChatPage />
            </ProtectedRoute>
          }
        />

        {/* Routes protégées avec layout (navbar + contenu) */}
        <Route
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/planifier" element={<PlanifierPage />} />
          <Route path="/salons" element={<SalonsPage />} />
          <Route path="/salons/invites/:chatId" element={<GestionInvitationsPage />} />
          <Route path="/invitations" element={<InvitationsPage />} />
          <Route path="/profil" element={<ProfilePage />} />
        </Route>

        {/* Fallback vers /home pour toute route inconnue */}
        <Route path="*" element={<Navigate to="/home" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
