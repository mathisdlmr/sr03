package fr.utc.sr03.websocket;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.security.JwtUtil;
import fr.utc.sr03.services.ChatService;
import fr.utc.sr03.services.InvitationService;
import fr.utc.sr03.services.UserService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WebSocketHandler extends TextWebSocketHandler {

    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    // chatId -> Set de sessions connectées à ce salon ("qui est connecté à quel salon ?")
    private final Map<String, Set<WebSocketSession>> chatSessions = new ConcurrentHashMap<>();
    // sessionId -> infos utilisateurs connectés ("cette session appartient à quel utilisateur ?")
    private final Map<String, Users> sessionUsers = new ConcurrentHashMap<>();
    // sessionId -> chatId ("cette session est connectée à quel salon ?")
    private final Map<String, String> sessionChatIds = new ConcurrentHashMap<>();

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ChatService chatService;
    private final InvitationService invitationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public WebSocketHandler(JwtUtil jwtUtil, UserService userService, ChatService chatService, InvitationService invitationService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.chatService = chatService;
        this.invitationService = invitationService;
    }

    // Extrait le chatId depuis l'URL de la WebSocket (par exemple : /ws/chat/42 -> "42")
    private String extractChatId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }

    // Extrait le token JWT depuis les query params de l'URL WebSocket (par exemple : ?token=xxx)
    // Le token que l'on cherche est la valeur du param d'URL "token"
    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String query = uri.getQuery();
        for (String param : query.split("&")) {
            String[] key_value = param.split("=", 2);
            if (key_value.length == 2 && "token".equals(key_value[0])) {
                return key_value[1];
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String chatId = extractChatId(session);
        String token = extractToken(session);

        // Vérification sur l'authentification
        if (token == null || !jwtUtil.isTokenValid(token) || jwtUtil.isRefreshToken(token)) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(MessageSocket.system("Authentification échouée."))));
            session.close();
            return;
        }

        // Vérification sur le user
        String email = jwtUtil.extractEmail(token);
        Users user = userService.getUserByEmailAddress(email);
        if (user == null || !user.isActive()) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(MessageSocket.system("Utilisateur introuvable ou désactivé."))));
            session.close();
            return;
        }

        // Vérification sur les droits d'accès au salon de chat
        if (chatId != null) {
            try {
                int chatIdInt = Integer.parseInt(chatId);
                boolean isOwner = chatService.isOwner(chatIdInt, user.getId());
                boolean isInvited = invitationService.isInvited(chatIdInt, user.getId());
                if (!isOwner && !isInvited) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(MessageSocket.system("Vous n'êtes pas autorisé à accéder à ce salon."))));
                    session.close();
                    return;
                }
            } catch (NumberFormatException e) {
                session.close(CloseStatus.BAD_DATA);
                return;
            }
        }

        // Si tout est bon, on enregistre la session
        sessionUsers.put(session.getId(), user);
        sessionChatIds.put(session.getId(), chatId);

        Set<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions == null) {
            sessions = ConcurrentHashMap.newKeySet();
            chatSessions.put(chatId, sessions);
        }
        sessions.add(session);

        // Puis on notifie les autres connectés du salon que ce user a rejoint le chat
        logger.info("Utilisateur " + user.getFirstname() + " " + user.getLastname() + " connecté au salon " + chatId);
        broadcastToChat(chatId, MessageSocket.system(user.getFirstname() + " " + user.getLastname() + " a rejoint le salon."));
        broadcastUserList(chatId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String chatId = sessionChatIds.get(session.getId());
        Users user = sessionUsers.get(session.getId());
        if (chatId == null || user == null) {
            return;
        }

        // On lit le message envoyé par le client, qui est au format JSON
        String payload = (String) message.getPayload();
        MessageSocket incoming = mapper.readValue(payload, MessageSocket.class);

        if ("kick".equals(incoming.getType())) {
            handleKick(chatId, user, incoming.getUserId());
            return;
        }

        // Puis on créé un nouveau message avec le payload et toutes les infos du user
        MessageSocket outgoing = new MessageSocket();
        outgoing.setUser(user.getFirstname() + " " + user.getLastname());
        outgoing.setUserId(user.getId());
        outgoing.setAvatar(user.getAvatar());
        outgoing.setTimestamp(System.currentTimeMillis());

        if ("image".equals(incoming.getType())) {
            outgoing.setType("image");
            outgoing.setImageData(incoming.getImageData());
        } else {
            outgoing.setType("text");
            outgoing.setMessage(incoming.getMessage());
        }

        // On broadcast finalement le message à tous les connectés du salon
        broadcastToChat(chatId, outgoing);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        // Lorsqu'une session se déconnecte, on la retire de nos listes de sessions connectées
        String chatId = sessionChatIds.remove(session.getId());
        Users user = sessionUsers.remove(session.getId());

        if (chatId != null) {
            Set<WebSocketSession> sessions = chatSessions.get(chatId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    chatSessions.remove(chatId);
                }
            }

            if (user != null) {
                // Puis on notifie les autres connectés du salon que ce user a quitté le chat
                logger.info("Utilisateur " + user.getFirstname() + " " + user.getLastname() + " déconnecté du salon " + chatId);
                broadcastToChat(chatId, MessageSocket.system(user.getFirstname() + " " + user.getLastname() + " a quitté le salon."));
                broadcastUserList(chatId);
            }
        }
    }

    // Envoie un message à tous les connectés d'un salon
    private void broadcastToChat(String chatId, MessageSocket message) throws IOException {
        Set<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions == null) {
            return;
        }

        // On convertit le message en JSON 
        String json = mapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(json);

        // Puis on l'envoie à toutes les sessions connectées au salon
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    logger.warning("Erreur lors de l'envoi au session " + session.getId() + " : " + e.getMessage());
                }
            }
        }
    }

    // Exclut un utilisateur du salon : seul le créateur du salon ou un administrateur peut le faire
    private void handleKick(String chatId, Users requester, int targetUserId) throws IOException {
        boolean isOwner = chatService.isOwner(Integer.parseInt(chatId), requester.getId());
        if (!isOwner && !requester.isAdmin()) {
            return;
        }

        Set<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions == null) {
            return;
        }

        for (WebSocketSession s : new HashSet<>(sessions)) {
            Users target = sessionUsers.get(s.getId());
            if (target != null && target.getId() == targetUserId && s.isOpen()) {
                logger.info("Utilisateur " + target.getFirstname() + " " + target.getLastname() + " exclu du salon " + chatId + " par " + requester.getFirstname() + " " + requester.getLastname());
                s.sendMessage(new TextMessage(mapper.writeValueAsString(MessageSocket.system("Vous avez été exclu du salon par " + requester.getFirstname() + " " + requester.getLastname() + "."))));
                s.close(CloseStatus.NORMAL);
            }
        }
    }

    // Envoie la liste des utilisateurs connectés à tous les connectés d'un salon
    private void broadcastUserList(String chatId) throws IOException {
        Set<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions == null) {
            return;
        }

        // On construit la liste des utilisateurs connectés au salon
        List<MessageSocket.ConnectedUser> connectedUsers = new ArrayList<>();
        for (WebSocketSession s : sessions) {
            Users u = sessionUsers.get(s.getId());
            if (u != null) {
                connectedUsers.add(new MessageSocket.ConnectedUser(u.getId(), u.getFirstname(), u.getLastname(), u.getAvatar(), u.isAdmin()));
            }
        }

        // Puis on broadcast cette liste à tous les connectés du salon
        broadcastToChat(chatId, MessageSocket.userList(connectedUsers));
    }
}
