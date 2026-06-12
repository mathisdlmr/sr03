import { useEffect, useState, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { formatTime } from '../utils/dateUtils';
import { getChat } from '../api/apiCalls';


export default function ChatPage() {
  const { chatId } = useParams();
  const { user } = useAuth();

  const [chat, setChat]     = useState({});
  const [loading, setLoading] = useState(true);
  const [messages, setMessages] = useState([]);
  const [connectedUsers, setConnectedUsers] = useState([]);
  const [message, setMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');
  const [openMenuId, setOpenMenuId] = useState(null);

  const wsRef = useRef(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  // Scroll automatique vers le bas à chaque nouveau message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // On ferme le menu ("...") à côté d'un utilisateur s'il est ouvert et qu'on clique en dehors
  useEffect(() => {
    if (!openMenuId) {
      return;
    }
    const closeMenu = () => setOpenMenuId(null);
    document.addEventListener('click', closeMenu);
    return () => document.removeEventListener('click', closeMenu);
  }, [openMenuId]);

  // Lorsque le composant est monté, on établit la connexion WebSocket au salon de chat
  useEffect(() => {
    const accessToken = localStorage.getItem('access_token');
    if (!accessToken || !chatId) {
      setError('Chat non trouvé.');
      return;
    }

    // On récupère le chat
    getChat(chatId).then((data) => setChat(data))

    // On construit l'URL du WebSocket en fonction de l'environnement (ws:// en dev, wss:// en prod)
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.hostname;
    const wsPort = '8080';
    const wsUrl = `${wsProtocol}//${wsHost}:${wsPort}/ws/chat/${chatId}?token=${accessToken}`;

    let cancelled = false;
    let websocket = null;

    const timer = setTimeout(() => {
      if (cancelled) {
        return;
      }

      websocket = new WebSocket(wsUrl);
      websocket.onopen = () => {
        setConnected(true);
        setLoading(false);
        setError('');
      };

      websocket.onmessage = (evt) => {
        try {
          const data = JSON.parse(evt.data);
          if (data.type === 'user_list') {
            setConnectedUsers(data.connectedUsers || []);
          } else {
            setMessages((prev) => [...prev, data]);
          }
        } catch {
          setMessages((prev) => [...prev, { type: 'text', message: evt.data, timestamp: Date.now() }]);
        }
      };

      websocket.onclose = () => {
        setConnected(false);
      };

      websocket.onerror = () => {
        setError('Erreur de connexion au salon...');
        setConnected(false);
      };

      wsRef.current = websocket;
    }, 0);

    return () => {
      cancelled = true;
      clearTimeout(timer);
      websocket?.close();
    };
  }, [chatId]);

  // Fonction pour envoyer un message :
  // on vérifie que le message n'est pas vide et que la WebSocket est ouverte avant d'envoyer les données au serveur
  const sendMessage = useCallback(() => {
    if (!message.trim() || !wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      return;
    }

    wsRef.current.send(JSON.stringify({
      type: 'text',
      message: message.trim(),
    }));
    setMessage('');
  }, [message]);

  // Permet d'envoyer le message en appuyant sur la touche "Entrée" (sans Shift, comme dans Messenger ou Slack sur ordinateur)
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Fonction pour envoyer une image :
  // on vérifie que le fichier est bien une image et qu'il ne dépasse pas 5 Mo avant de le lire en base64 et de l'envoyer au serveur
  const handleImageSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      alert('Seules les images sont acceptées');
      return;
    }
    if (file.size > 5 * 1024 * 1024) { // 5 Mo max, hypothèse subjective (on veut simplement éviter de recevoir un image de 500Mo...)
      alert("L'image ne doit pas dépasser 5 Mo");
      return;
    }

    // On lit le fichier en base64 pour pouvoir l'envoyer directement dans la WebSocket
    const reader = new FileReader();
    reader.onload = () => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send(JSON.stringify({
          type: 'image',
          imageData: reader.result,
        }));
      }
    };
    reader.readAsDataURL(file);
    e.target.value = ''; // On réinitialise l'input à la fin
  };

  // Exclut un utilisateur connecté du salon (réservé au créateur et aux admins)
  const handleKick = (targetUserId) => {
    setOpenMenuId(null);
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      return;
    }
    if (!confirm('Exclure cet utilisateur du salon ?')) {
      return;
    }
    wsRef.current.send(JSON.stringify({
      type: 'kick',
      userId: targetUserId,
    }));
  };

  // Le créateur du salon et les administrateurs peuvent exclure d'autres utilisateurs connectés
  const isPrivileged = !!user && (user.admin || user.id === chat.creator_id);

  return (
    <div>
        {loading ? (
            <div className="d-flex flex-justify-center p-10">
              <span className="mif-spinner2 ani-spin mif-3x fg-blue" />
            </div>
        ) : (
            <div className="d-flex" style={{ height: '100vh', fontFamily: "'DM Sans', sans-serif" }}>
              <div className="d-flex flex-column" style={{ flex: 1 }}>
                <div className="d-flex flex-align-center flex-justify-between p-3 bg-light" style={{ borderBottom: '1px solid #ddd' }}>
                  <div>
                    <h3 className="m-0">
                      <span className="mif-chat mr-2 fg-blue" />
                      Salon #{chatId} :  {chat.title}
                    </h3>
                  </div>
                  <div>
                    {connected ? (
                        <span className="badge inline bg-green fg-white">
                  <span className="mif-checkmark mr-1" /> Connecté
                </span>
                    ) : (
                        <span className="badge inline bg-red fg-white">
                  <span className="mif-cancel mr-1" /> Déconnecté
                </span>
                    )}
                  </div>
                </div>

                <div className="p-3" style={{ flex: 1, overflowY: 'auto', backgroundColor: '#fafafa' }}>
                  {error && (
                      <div className="alert alert-warning border-radius-2 mb-4">
                        <span className="mif-warning mx-2" />{error}
                      </div>
                  )}

                  {messages.length === 0 && connected && (
                      <div className="text-center text-muted p-10">
                        <span className="mif-bubbles mif-4x d-block mb-4" />
                        <p>Aucun message pour l'instant. Écrivez le premier !</p>
                      </div>
                  )}

                  {messages.map((msg, index) => {
                    if (msg.type === 'system') {
                      return (
                          <div key={index} className="text-center my-4">
                    <span className="text-muted" style={{ fontSize: '12px', fontStyle: 'italic' }}>
                      {msg.message} — {formatTime(msg.timestamp)}
                    </span>
                          </div>
                      );
                    }

                    const isMe = msg.userId === user?.id;

                    return (
                        <div
                            key={index}
                            className={`d-flex mb-3 ${isMe ? 'flex-justify-end' : 'flex-justify-start'}`}
                        >
                          {!isMe && (
                              <div className="mr-2" style={{ flexShrink: 0 }}>
                                {msg.avatar ? (
                                    <img src={msg.avatar} alt="" style={{ width: 36, height: 36, borderRadius: '50%', objectFit: 'cover' }} />
                                ) : (
                                    <div className="d-flex flex-align-center flex-justify-center bg-blue fg-white text-bold" style={{
                                      width: 36, height: 36, borderRadius: '50%', fontSize: 14,
                                    }}>
                                      {msg.user?.[0] || '?'}
                                    </div>
                                )}
                              </div>
                          )}

                          <div style={{ maxWidth: '70%' }}>
                            {!isMe && (
                                <div className="text-muted mb-1" style={{ fontSize: '12px' }}>{msg.user}</div>
                            )}
                            <div className={`shadow-normal ${isMe ? 'bg-blue fg-white' : 'bg-white border border-size-1 bd-gray'}`} style={{
                              padding: msg.type === 'image' ? '6px' : '10px 14px',
                              borderRadius: isMe ? '16px 16px 0 16px' : '16px 16px 16px 0',
                              wordBreak: 'break-word',
                            }}>
                              {msg.type === 'image' ? (
                                  <img
                                      src={msg.imageData}
                                      alt="Image partagée"
                                      style={{ maxWidth: '100%', maxHeight: 300, borderRadius: 10, cursor: 'pointer' }}
                                      onClick={() => window.open(msg.imageData, '_blank')}
                                  />
                              ) : (
                                  msg.message
                              )}
                            </div>
                            <div className="text-muted mt-1" style={{ fontSize: '10px', textAlign: isMe ? 'right' : 'left' }}>
                              {formatTime(msg.timestamp)}
                            </div>
                          </div>
                        </div>
                    );
                  })}
                  <div ref={messagesEndRef} />
                </div>

                <div className="d-flex flex-align-center gap-2 p-3 bg-light" style={{ borderTop: '1px solid #ddd' }}>
                  <input
                      type="file"
                      ref={fileInputRef}
                      style={{ display: 'none' }}
                      accept="image/*"
                      onChange={handleImageSelect}
                  />
                  <button
                      className="button small"
                      onClick={() => fileInputRef.current?.click()}
                      title="Envoyer une image"
                      disabled={!connected}
                  >
                    <span className="mif-images" />
                  </button>

                  <input
                      type="text"
                      style={{ flex: 1 }}
                      placeholder="Écrivez votre message..."
                      value={message}
                      onChange={(e) => setMessage(e.target.value)}
                      onKeyDown={handleKeyDown}
                      disabled={!connected}
                  />

                  <button
                      className="button info"
                      onClick={sendMessage}
                      disabled={!connected || !message.trim()}
                  >
                    <span className="mif-paper-plane mr-1" /> Envoyer
                  </button>
                </div>
              </div>
              <div className="d-flex flex-column bg-light" style={{ width: 240, borderLeft: '1px solid #ddd' }}>
                <div className="p-3" style={{ borderBottom: '1px solid #ddd' }}>
                  <h4 className="m-0">
                    <span className="mif-users mr-2" />
                    Connectés ({connectedUsers.length})
                  </h4>
                </div>
                <div className="p-2" style={{ flex: 1, overflowY: 'auto' }}>
                  {connectedUsers.map((u) => (
                      <div
                          key={u.id}
                          className="d-flex flex-align-center flex-justify-between mb-3 pos-relative"
                      >
                        <div className="d-flex flex-align-center" style={{ minWidth: 0 }}>
                          {u.avatar ? (
                              <img src={u.avatar} alt="" className="mr-2" style={{ width: 28, height: 28, borderRadius: '50%', objectFit: 'cover', flexShrink: 0 }} />
                          ) : (
                              <div className="d-flex flex-align-center flex-justify-center bg-blue fg-white text-bold mr-2" style={{
                                width: 28, height: 28, borderRadius: '50%', fontSize: 12, flexShrink: 0,
                              }}>
                                {u.firstname?.[0] || '?'}
                              </div>
                          )}
                          <div style={{ minWidth: 0, overflow: 'hidden' }}>
                            <span style={{ fontSize: 13 }}>
                              {u.firstname} {u.lastname}
                            </span>
                            <div>
                              {u.id === chat.creator_id && (
                                  <span className="badge inline bg-violet fg-white mr-1" style={{ fontSize: 10 }}>Creator</span>
                              )}
                              {u.admin && (
                                  <span className="badge inline bg-blue fg-white" style={{ fontSize: 10 }}>Admin</span>
                              )}
                            </div>
                          </div>
                        </div>

                        {isPrivileged && u.id !== user?.id && (
                            <div className="pos-relative">
                              <button
                                  className="button square small"
                                  title="Actions"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setOpenMenuId(openMenuId === u.id ? null : u.id);
                                  }}
                              >
                                <span className="mif-more-vert" />
                              </button>
                              {openMenuId === u.id && (
                                  <ul className="d-menu shadow-normal" style={{ display: 'block', right: 0, top: '100%' }}>
                                    <li>
                                      <a href="#" onClick={(e) => { e.preventDefault(); handleKick(u.id); }}>
                                        <span className="mif-remove-person mr-1" /> Exclure
                                      </a>
                                    </li>
                                  </ul>
                              )}
                            </div>
                        )}
                      </div>
                  ))}
                </div>
              </div>
            </div>
        )}
    </div>
  );
}
