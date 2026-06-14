import { useEffect, useState, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { formatTime, formatDuration } from '../utils/dateUtils';
import { getChat } from '../api/apiCalls';
import VoiceMessageBubble from '../components/VoiceMessageBubble';

export default function ChatPage() {
  const { chatId } = useParams();
  const { user } = useAuth();

  const [chat, setChat] = useState({});
  const [loading, setLoading] = useState(true);
  const [messages, setMessages] = useState([]);
  const [connectedUsers, setConnectedUsers] = useState([]);
  const [message, setMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState(() =>
    localStorage.getItem('access_token') && chatId ? '' : 'Chat non trouvé.'
  );
  const [openMenuId, setOpenMenuId] = useState(null);
  const [isRecording, setIsRecording] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [recordingSeconds, setRecordingSeconds] = useState(0);

  const wsRef = useRef(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const fileAttachInputRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunks = useRef([]);
  const cancelRecordingRef = useRef(false);

  // Scroll automatique vers le bas à chaque nouveau message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Met à jour le titre de l'onglet du navigateur avec le nom du salon
  useEffect(() => {
    if (chat?.title) {
      document.title = chat.title;
    }
  }, [chat?.title]);

  // Timer utilisé pour compter le nombre de secondes écoulées depuis le début d'un vocal
  useEffect(() => {
    if (!isRecording) {
      return;
    }
    let seconds = 0;
    const interval = setInterval(() => {
      seconds += 1;
      setRecordingSeconds(seconds);
    }, 1000);
    return () => clearInterval(interval);
  }, [isRecording]);

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
      return;
    }

    // On récupère le chat
    getChat(chatId).then(data => setChat(data));

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

      websocket.onmessage = evt => {
        try {
          const data = JSON.parse(evt.data);
          if (data.type === 'user_list') {
            setConnectedUsers(data.connectedUsers || []);
          } else {
            setMessages(prev => [...prev, data]);
          }
        } catch {
          setMessages(prev => [
            ...prev,
            { type: 'text', message: evt.data, timestamp: Date.now() },
          ]);
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

    wsRef.current.send(
      JSON.stringify({
        type: 'text',
        message: message.trim(),
      })
    );
    setMessage('');
  }, [message]);

  // Permet d'envoyer le message en appuyant sur la touche "Entrée" (sans Shift, comme dans Messenger ou Slack sur ordinateur)
  const handleKeyDown = e => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Fonction pour envoyer une image (on le sépare des autres fichiers pour afficher une preview de l'image dans le chat):
  // on vérifie que le fichier est bien une image et qu'il ne dépasse pas 5 Mo avant de le lire en base64 et de l'envoyer au serveur
  const handleImageSelect = e => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      alert('Seules les images sont acceptées');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      // 5 Mo max, hypothèse subjective (on veut simplement éviter de recevoir un image de 500Mo...)
      alert("L'image ne doit pas dépasser 5 Mo");
      return;
    }

    // On lit le fichier en base64 pour pouvoir l'envoyer directement dans la WebSocket
    const reader = new FileReader();
    reader.onload = () => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send(
          JSON.stringify({
            type: 'image',
            imageData: reader.result,
          })
        );
      }
    };
    reader.readAsDataURL(file);
    e.target.value = ''; // On réinitialise l'input à la fin
  };

  // Fonction pour envoyer un fichier quelconque :
  // on vérifie simplement la taille avant de le lire en base64 et de l'envoyer au serveur
  const handleFileSelect = e => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      // 5 Mo max, même limite que pour les images
      alert('Le fichier ne doit pas dépasser 5 Mo');
      return;
    }

    // Même idée que pour les images : lire en base64 et envoyer sur la WebSocket
    const reader = new FileReader();
    reader.onload = () => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send(
          JSON.stringify({
            type: 'file',
            fileData: reader.result,
            fileName: file.name,
          })
        );
      }
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  };

  // Démarre l'enregistrement d'un message vocal via le micro
  // Ce code a été récupéré et adapté depuis https://stackoverflow.com/questions/78825337/how-to-record-audio-in-react
  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream);

      audioChunks.current = [];
      cancelRecordingRef.current = false;

      recorder.ondataavailable = e => {
        if (e.data.size > 0) {
          audioChunks.current.push(e.data);
        }
      };

      recorder.onstop = () => {
        stream.getTracks().forEach(track => track.stop());
        if (cancelRecordingRef.current) {
          return;
        }
        const blob = new Blob(audioChunks.current, { type: recorder.mimeType }); // Ici on a notre audio en Blob (binaire)
        const reader = new FileReader(); // On va donc le convertir en base64 (comme pour le reste) pour l'envoyer sur notre WebSocket
        reader.onload = () => {
          if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            wsRef.current.send(
              JSON.stringify({
                type: 'audio',
                audioData: reader.result,
              })
            );
          }
        };
        reader.readAsDataURL(blob);
      };

      recorder.start();
      mediaRecorderRef.current = recorder;
      setRecordingSeconds(0);
      setIsRecording(true);
    } catch {
      alert("Impossible d'accéder au microphone...");
    }
  };

  // Arrête l'enregistrement et envoie le message vocal
  const sendRecording = () => {
    cancelRecordingRef.current = false;
    mediaRecorderRef.current?.stop();
    setIsRecording(false);
  };

  // Arrête l'enregistrement sans envoyer le message vocal
  const cancelRecording = () => {
    cancelRecordingRef.current = true;
    mediaRecorderRef.current?.stop();
    setIsRecording(false);
  };

  // Exclut un utilisateur connecté du salon (réservé au créateur et aux admins)
  const handleKick = targetUserId => {
    setOpenMenuId(null);
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      return;
    }
    if (!confirm('Exclure cet utilisateur du salon ?')) {
      return;
    }
    wsRef.current.send(
      JSON.stringify({
        type: 'kick',
        userId: targetUserId,
      })
    );
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
            <div
              className="d-flex flex-align-center flex-justify-between p-3 bg-light"
              style={{ borderBottom: '1px solid #ddd', flexWrap: 'wrap', gap: 8 }}
            >
              <div style={{ minWidth: 0 }}>
                <h3
                  className="m-0"
                  style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                >
                  <span className="mif-chat mr-2 fg-blue" />
                  Salon #{chatId} : {chat.title}
                </h3>
              </div>
              <div className="d-flex flex-align-center gap-2">
                {connected ? (
                  <span
                    className="badge inline bg-green fg-white p-2"
                    style={{ fontSize: '0.8rem' }}
                  >
                    <span className="mif-checkmark mr-1" style={{ fontSize: '0.9rem' }} /> Connecté
                  </span>
                ) : (
                  <span className="badge inline bg-red fg-white p-2" style={{ fontSize: '0.8rem' }}>
                    <span className="mif-cancel mr-1" style={{ fontSize: '0.9rem' }} /> Déconnecté
                  </span>
                )}
                <button
                  className="button square small chat-sidebar-toggle"
                  title="Utilisateurs connectés"
                  onClick={() => setSidebarOpen(true)}
                >
                  <span className="mif-users" />
                </button>
              </div>
            </div>

            <div className="p-3" style={{ flex: 1, overflowY: 'auto', backgroundColor: '#fafafa' }}>
              {error && (
                <div className="alert alert-warning border-radius-2 mb-4">
                  <span className="mif-warning mx-2" />
                  {error}
                </div>
              )}

              {messages.length === 0 && connected && (
                <div className="text-center text-muted p-10">
                  <span className="mif-bubbles mif-4x d-block mb-4" />
                  <p>Aucun message pour l&apos;instant. Écrivez le premier !</p>
                </div>
              )}

              {messages.map((msg, index) => {
                if (msg.type === 'system') {
                  return (
                    <div key={index} className="text-center my-4">
                      <span
                        className="text-muted"
                        style={{ fontSize: '12px', fontStyle: 'italic' }}
                      >
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
                      <div className="mr-2 d-flex flex-column justify-content-end">
                        {msg.avatar ? (
                          <img
                            src={msg.avatar}
                            alt=""
                            style={{
                              width: 36,
                              height: 36,
                              borderRadius: '50%',
                              objectFit: 'cover',
                            }}
                            className="mt-auto"
                          />
                        ) : (
                          <div
                            className="d-flex flex-align-center flex-justify-center bg-blue fg-white text-bold mt-auto"
                            style={{
                              width: 36,
                              height: 36,
                              borderRadius: '50%',
                              fontSize: 14,
                            }}
                          >
                            {msg.user?.[0] || '?'}
                          </div>
                        )}
                      </div>
                    )}

                    <div className="chat-message-bubble" style={{ maxWidth: '70%' }}>
                      {!isMe && (
                        <div className="text-muted mb-1" style={{ fontSize: '12px' }}>
                          {msg.user}
                        </div>
                      )}
                      <div
                        className={`shadow-normal ${isMe ? 'bg-blue fg-white' : 'bg-white border border-size-1 bd-gray'}`}
                        style={{
                          padding: msg.type === 'image' ? '6px' : '10px 14px',
                          borderRadius: isMe ? '16px 16px 0 16px' : '16px 16px 16px 0',
                          wordBreak: 'break-word',
                        }}
                      >
                        {msg.type === 'image' ? (
                          <img
                            src={msg.imageData}
                            alt="Image partagée"
                            style={{
                              maxWidth: '100%',
                              maxHeight: 300,
                              borderRadius: 10,
                              cursor: 'pointer',
                            }}
                            onClick={() => window.open(msg.imageData, '_blank')}
                          />
                        ) : msg.type === 'file' ? (
                          <a
                            href={msg.fileData}
                            download={msg.fileName}
                            className={`d-flex flex-align-center ${isMe ? 'fg-white' : 'fg-dark'}`}
                            style={{ gap: 8, textDecoration: 'none' }}
                          >
                            <span className="mif-file-present mif-2x" />
                            <span style={{ wordBreak: 'break-all' }}>{msg.fileName}</span>
                          </a>
                        ) : msg.type === 'audio' ? (
                          <VoiceMessageBubble src={msg.audioData} isMe={isMe} />
                        ) : (
                          msg.message
                        )}
                      </div>
                      <div
                        className="text-muted mt-1"
                        style={{ fontSize: '10px', textAlign: isMe ? 'right' : 'left' }}
                      >
                        {formatTime(msg.timestamp)}
                      </div>
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>

            <div
              className="d-flex flex-align-center gap-2 p-3 bg-light"
              style={{ borderTop: '1px solid #ddd' }}
            >
              <input
                type="file"
                ref={fileInputRef}
                style={{ display: 'none' }}
                accept="image/*"
                onChange={handleImageSelect}
              />
              <input
                type="file"
                ref={fileAttachInputRef}
                style={{ display: 'none' }}
                onChange={handleFileSelect}
              />

              {isRecording ? (
                <>
                  <button
                    className="button small"
                    onClick={cancelRecording}
                    title="Annuler"
                    style={{ height: '35px' }}
                  >
                    <span className="mif-bin" />
                  </button>

                  <div
                    className="d-flex flex-align-center flex-justify-center gap-2"
                    style={{ flex: 1, height: 36 }}
                  >
                    <span className="mif-mic fg-red ani-flash" />
                    <span>{formatDuration(recordingSeconds)}</span>
                  </div>
                  <button
                    className="button info"
                    onClick={sendRecording}
                    title="Envoyer le message vocal"
                    style={{ height: '35px' }}
                  >
                    <span className="mif-paper-plane mr-1" />{' '}
                    <span className="chat-send-label">Envoyer</span>
                  </button>
                </>
              ) : (
                <>
                  <button
                    className="button small"
                    onClick={() => fileAttachInputRef.current?.click()}
                    title="Envoyer un fichier"
                    disabled={!connected}
                    style={{ height: '35px' }}
                  >
                    <span className="mif-folder-open" />
                  </button>
                  <button
                    className="button small"
                    onClick={() => fileInputRef.current?.click()}
                    title="Envoyer une image"
                    disabled={!connected}
                    style={{ height: '35px' }}
                  >
                    <span className="mif-images" />
                  </button>

                  <button
                    className="button small"
                    onClick={startRecording}
                    title="Message vocal"
                    disabled={!connected}
                    style={{ height: '35px' }}
                  >
                    <span className="mif-mic" />
                  </button>

                  <input
                    type="text"
                    style={{ flex: 1, height: '35px' }}
                    placeholder="Écrivez votre message..."
                    value={message}
                    onChange={e => setMessage(e.target.value)}
                    onKeyDown={handleKeyDown}
                    disabled={!connected}
                  />

                  <button
                    className="button info"
                    onClick={sendMessage}
                    disabled={!connected || !message.trim()}
                    style={{ height: '35px' }}
                  >
                    <span className="mif-paper-plane mr-1" />{' '}
                    <span className="chat-send-label">Envoyer</span>
                  </button>
                </>
              )}
            </div>
          </div>
          <div
            className={`chat-sidebar-backdrop ${sidebarOpen ? 'open' : ''}`}
            onClick={() => setSidebarOpen(false)}
          />
          <div
            className={`chat-sidebar d-flex flex-column bg-light ${sidebarOpen ? 'open' : ''}`}
            style={{ width: 240, borderLeft: '1px solid #ddd' }}
          >
            <div
              className="p-3 d-flex flex-align-center flex-justify-between"
              style={{ borderBottom: '1px solid #ddd' }}
            >
              <h4 className="m-0">
                <span className="mif-users mr-2" />
                Connectés ({connectedUsers.length})
              </h4>
              <button
                className="button square small chat-sidebar-toggle"
                title="Fermer"
                onClick={() => setSidebarOpen(false)}
              >
                <span className="mif-cross" />
              </button>
            </div>
            <div className="p-2" style={{ flex: 1, overflowY: 'auto' }}>
              {connectedUsers.map(u => (
                <div
                  key={u.id}
                  className="d-flex flex-align-center flex-justify-between mb-3 pos-relative"
                >
                  <div className="d-flex flex-align-center" style={{ minWidth: 0 }}>
                    {u.avatar ? (
                      <img
                        src={u.avatar}
                        alt=""
                        className="mr-2"
                        style={{
                          width: 28,
                          height: 28,
                          borderRadius: '50%',
                          objectFit: 'cover',
                          flexShrink: 0,
                        }}
                      />
                    ) : (
                      <div
                        className="d-flex flex-align-center flex-justify-center bg-blue fg-white text-bold mr-2"
                        style={{
                          width: 28,
                          height: 28,
                          borderRadius: '50%',
                          fontSize: 12,
                          flexShrink: 0,
                        }}
                      >
                        {u.firstname?.[0] || '?'}
                      </div>
                    )}
                    <div style={{ minWidth: 0, overflow: 'hidden' }}>
                      <span style={{ fontSize: 13 }}>
                        {u.firstname} {u.lastname}
                      </span>
                      <div>
                        {u.id === chat.creator_id && (
                          <span
                            className="badge inline bg-violet fg-white mr-1"
                            style={{ fontSize: 12 }}
                          >
                            Creator
                          </span>
                        )}
                        {u.admin && (
                          <span className="badge inline bg-blue fg-white" style={{ fontSize: 12 }}>
                            Admin
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {isPrivileged && u.id !== user?.id && (
                    <div className="pos-relative">
                      <button
                        className="button square small"
                        title="Actions"
                        onClick={e => {
                          e.stopPropagation();
                          setOpenMenuId(openMenuId === u.id ? null : u.id);
                        }}
                      >
                        <span className="mif-more-vert" />
                      </button>
                      {openMenuId === u.id && (
                        <ul
                          className="d-menu shadow-normal"
                          style={{ display: 'block', right: 0, top: '100%' }}
                        >
                          <li>
                            <a
                              href="#"
                              onClick={e => {
                                e.preventDefault();
                                handleKick(u.id);
                              }}
                            >
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
