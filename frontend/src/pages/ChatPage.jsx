import { useEffect, useState, useRef } from 'react'
import { useParams } from "react-router";
import {useAuth} from "../hooks/useAuth.js";
import {getChat} from '../api/chatApi';
import {Link} from "react-router-dom";

export default function ChatPageWebsocket() {
  let chatId = Number(useParams().chatId);
  console.log('ChatId : '+chatId);

  const [chat, setChat]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  const loadChat = () => {
    setLoading(true);
    getChat(chatId).then((data) => setChat(data)).catch(() => setError('Impossible de charger le salon...')).finally(() => setLoading(false));
  };
  useEffect(() => { loadChat(); }, []);

  const { user } = useAuth();

  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');

  //  Utilisation de useRef à la place de useState pour le WebSocket
  const wsRef = useRef(null);

  useEffect(() => {
    const websocket = new WebSocket('ws://localhost:8080/salon?chatId='+String(chatId));

    websocket.onopen = () => {
      console.log('WebSocket is connected');
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send(JSON.stringify({
          type: 'join',
          chat: chatId,
          user: user.firstname + " " + user.lastname,
          message: 'a rejoint le chat.',
          msgtimestamp : Date.now(),
        }));

        setMessage('');
      } else {
        console.warn("Le WebSocket n'est pas prêt ou est déconnecté.");
      }
    };

    websocket.onmessage = (evt) => {
      const message = evt.data;
      console.log('Msg :'+message);
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
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN && message) {
      wsRef.current.send(JSON.stringify({
        type: 'message',
        chat: chatId,
        user: user.firstname + " " + user.lastname,
        message: message,
        msgtimestamp : Date.now(),
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
      <div>
        <div>
          <div className="d-flex flex-justify-between flex-align-center mb-6">
            <h1 className="text-bold mb-1">Salon de discussion</h1>
          </div>

          {loading ? (
              <div className="d-flex flex-justify-center p-10">
                <span className="mif-spinner2 ani-spin mif-3x fg-blue" />
              </div>
          ) : error ? (
              <div className="alert alert-warning border-radius-2 mb-4">
                <span className="mif-warning mx-2" />{error}
              </div>
          ) : (
              <div>
                <div className="p-6 text-center">
                  <h2>
                    Salon : {chat.title}
                  </h2>
                  <p className="text-muted">Géré par : * insert nom owner chat * </p>
                </div>
                <div className="p-6 text-center">
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
                </div>
              </div>
          )}
        </div>
      </div>
  );
}