import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@olton/metroui/lib/metro.css';
import '@olton/metroui/lib/icons.css';
import '@olton/metroui/lib/metro.js';

import './index.css';
import { AuthProvider } from './contexts/AuthContext';
import App from './App';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>
);