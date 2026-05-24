import { useEffect, useState, useRef } from 'react'
import './App.css'

function App() {
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');

  //  Utilisation de useRef à la place de useState pour le WebSocket
  const wsRef = useRef(null);

  useEffect(() => {
    const websocket = new WebSocket('ws://localhost:8080/salon');

    websocket.onopen = () => {
      console.log('WebSocket is connected');
    };

    websocket.onmessage = (evt) => {
      const message = evt.data;
      setMessages((prevMessages) => [...prevMessages, message]);
    };

    websocket.onclose = () => {
      console.log('WebSocket is closed');
    };

    // On stocke la connexion dans le ref (pas de render provoqué)
    wsRef.current = websocket;

    return () => {
      websocket.close();
    };
  }, []);

  const sendMessage = () => {
    // On accède au WebSocket via wsRef.current
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({
        user: 'Cédric',
        message: message,
      }));

      setMessage('');
    } else {
      console.warn("Le WebSocket n'est pas prêt ou est déconnecté.");
    }
  };

  const handleInputChange = (event) => {
    setMessage(event.target.value);
  };

  return (
      <div className="App">
        <header className="App-header">
          <h1>
            Exemple SR03 - Cédric Martinet
          </h1>
          {messages.map((msg, index) => <p key={index}>{msg}</p>)}
          <input
              type="text"
              value={message}
              onChange={handleInputChange}
          />
          <br/>
          <button onClick={sendMessage}>
            Envoyer le message
          </button>
        </header>
      </div>
  );
}

export default App;
