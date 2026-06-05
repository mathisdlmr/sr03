import Navbar from '../components/Navbar';
import { Outlet } from 'react-router-dom';

// Layout principal de l'app, avec Navbar et espace pour les pages enfants (Outlet)
export default function AppLayout() {
  return (
    <div>
      <Navbar />
      <main className="container mt-6">
        <Outlet />
      </main>
    </div>
  );
}
