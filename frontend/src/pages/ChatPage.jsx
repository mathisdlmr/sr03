import { useEffect, useState, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function ChatPage() {
  const { chatId } = useParams();
  const { user } = useAuth();

  const [messages, setMessages] = useState([]);
  const [connectedUsers, setConnectedUsers] = useState([]);
  const [message, setMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');

  const wsRef = useRef(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    const accessToken = localStorage.getItem('access_token');
    if (!accessToken || !chatId) {
      setError('Chat non trouvé.');
      return;
    }

    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.hostname;
    const wsPort = '8080';
    const wsUrl = `${wsProtocol}//${wsHost}:${wsPort}/ws/chat/${chatId}?token=${accessToken}`;

    const websocket = new WebSocket(wsUrl);

    websocket.onopen = () => {
      setConnected(true);
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

    return () => {
      websocket.close();
    };
  }, [chatId]);

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

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleImageSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      alert('Seules les images sont acceptées.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) { // 5 Mo max
      alert('L\'image ne doit pas dépasser 5 Mo.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send(JSON.stringify({
          type: 'image',
          imageData: reader.result, // base64 data URI
        }));
      }
    };
    reader.readAsDataURL(file);
    // Reset le champ fichier
    e.target.value = '';
  };

  // Formater le timestamp
  const formatTime = (ts) => {
    if (!ts) {
      return '';
    }
    return new Date(ts).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: "'DM Sans', sans-serif" }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <div style={{
          padding: '12px 20px',
          borderBottom: '1px solid #ddd',
          backgroundColor: '#f8f9fa',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <div>
            <h3 style={{ margin: 0 }}>
              <span className="mif-chat mr-2 fg-blue" />
              Salon #{chatId}
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

        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '16px 20px',
          backgroundColor: '#fafafa',
        }}>
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
                style={{
                  display: 'flex',
                  justifyContent: isMe ? 'flex-end' : 'flex-start',
                  marginBottom: '12px',
                }}
              >
                {!isMe && (
                  <div style={{ marginRight: 8, flexShrink: 0 }}>
                    {msg.avatar ? (
                      <img src={msg.avatar} alt="" style={{ width: 36, height: 36, borderRadius: '50%', objectFit: 'cover' }} />
                    ) : (
                      <div style={{
                        width: 36, height: 36, borderRadius: '50%',
                        backgroundColor: '#4a90d9', color: '#fff',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: 14, fontWeight: 'bold',
                      }}>
                        {msg.user?.[0] || '?'}
                      </div>
                    )}
                  </div>
                )}

                <div style={{ maxWidth: '70%' }}>
                  {!isMe && (
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: 2 }}>{msg.user}</div>
                  )}
                  <div style={{
                    padding: msg.type === 'image' ? '6px' : '10px 14px',
                    borderRadius: isMe ? '16px 16px 0 16px' : '16px 16px 16px 0',
                    backgroundColor: isMe ? '#4a90d9' : '#fff',
                    color: isMe ? '#fff' : '#333',
                    border: isMe ? 'none' : '1px solid #e0e0e0',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
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
                  <div style={{ fontSize: '10px', color: '#999', marginTop: 2, textAlign: isMe ? 'right' : 'left' }}>
                    {formatTime(msg.timestamp)}
                  </div>
                </div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>

        <div style={{
          padding: '12px 20px',
          borderTop: '1px solid #ddd',
          backgroundColor: '#f8f9fa',
          display: 'flex',
          gap: '10px',
          alignItems: 'center',
        }}>
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

      <div style={{
        width: 220,
        borderLeft: '1px solid #ddd',
        backgroundColor: '#f8f9fa',
        display: 'flex',
        flexDirection: 'column',
      }}>
        <div style={{ padding: '12px 16px', borderBottom: '1px solid #ddd' }}>
          <h4 style={{ margin: 0 }}>
            <span className="mif-users mr-2" />
            Connectés ({connectedUsers.length})
          </h4>
        </div>
        <div style={{ flex: 1, overflowY: 'auto', padding: '8px 12px' }}>
          {connectedUsers.map((u) => (
            <div
              key={u.id}
              className="d-flex flex-align-center mb-3"
            >
              {u.avatar ? (
                <img src={u.avatar} alt="" style={{ width: 28, height: 28, borderRadius: '50%', objectFit: 'cover', marginRight: 8 }} />
              ) : (
                <div style={{
                  width: 28, height: 28, borderRadius: '50%',
                  backgroundColor: '#4a90d9', color: '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 12, fontWeight: 'bold', marginRight: 8, flexShrink: 0,
                }}>
                  {u.firstname?.[0] || '?'}
                </div>
              )}
              <span style={{ fontSize: 13 }}>
                {u.firstname} {u.lastname}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
