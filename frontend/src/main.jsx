import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { UserProvider } from './helpers/userContext';
import HomeScreen from './pages/home';
import LoginScreen from './pages/login';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserProvider>
      <App />
    </UserProvider>
  </StrictMode>,
)

function App() {
  // const { user } = useUser();
  const user = {
    id: 1,
    firstname: "Mathis",
    lastname: "Delmaere",
    mail: "mathis.delmaere@etu.utc.fr"
  };
  
  return (
    <>
      {user ? (
        <HomeScreen user={user} />
      ) : (
        <LoginScreen />
      )}
    </>
  );
}