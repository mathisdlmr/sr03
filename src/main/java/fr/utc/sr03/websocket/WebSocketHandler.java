package fr.utc.sr03.websocket;

import fr.utc.sr03.controller.ApiController;
import fr.utc.sr03.model.Users;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class WebSocketHandler extends TextWebSocketHandler {

    private final String nameChat;
    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    //private final List<WebSocketSession> sessions;
    private final Map<Integer, List<WebSocketSession>> chatSessions;
    //private final List<MessageSocket> messageSocketsHistory;
    private final Map<Integer, List<MessageSocket>> chatMessageSocketsHistory;

    public WebSocketHandler(String nameChat) {
        this.nameChat = nameChat;
        //this.messageSocketsHistory = new ArrayList<>();
        this.chatMessageSocketsHistory = new HashMap<>();
        //this.sessions = new ArrayList<>();
        this.chatSessions = new HashMap<>();
    }

    // Récupère l'utilisateur authentifié via le JWT
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return user;
        }
        return null;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String receivedMessage = (String) message.getPayload();
        logger.info("received msg : " + receivedMessage);
        MessageSocket messageSocket = mapper.readValue(receivedMessage, MessageSocket.class);
        logger.info("msg socket : " + messageSocket);
        logger.info("mapper.writeValueAsString(messageSocket) : " + mapper.writeValueAsString(messageSocket));


        logger.info("chatid from msg " + messageSocket.getChat());
        Integer chatId = messageSocket.getChat();
        // Pour stocker le message dans l'historique du chat
        List<MessageSocket> messageSocketsHistory = new ArrayList<>();
        if (chatMessageSocketsHistory.get(chatId)!=null) {
            messageSocketsHistory = chatMessageSocketsHistory.get(chatId);
        }
        messageSocketsHistory.add(messageSocket);
        chatMessageSocketsHistory.put(chatId, messageSocketsHistory);

        // Envoi du message à tous les connectés
        this.broadcast(mapper.writeValueAsString(messageSocket), chatId);

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // On stocke la session du client dans une liste pour le chat
        logger.info("session getUri " + session.getUri().getQuery());
        String query = session.getUri().getQuery();
        String[] queryParams = query.split("&");
        Map<String, String> params = new HashMap<>();
        for (String queryParam : queryParams) {
            String[] keyVal = queryParam.split("=",2);
            params.put(keyVal[0], keyVal[1]);
        }
        Integer chatId = Integer.valueOf(params.get("chatId"));
        session.getAttributes().put("chatId", chatId);

        List<WebSocketSession> sessions = new ArrayList<>();
        if (chatSessions.get(chatId)!=null) {
            sessions = chatSessions.get(chatId);
        }
        sessions.add(session);
        chatSessions.put(chatId, sessions);
        logger.info("sessions"+session.getId());

        // J'affiche l'historique du salon
        if (chatMessageSocketsHistory.get(chatId)!=null) {
            for (MessageSocket messageSocket : chatMessageSocketsHistory.get(chatId)) {
                ObjectMapper mapper = new ObjectMapper();
                session.sendMessage(new TextMessage(mapper.writeValueAsString(messageSocket)));
            }
        }

        logger.info("!!! Connecté sur le " + this.nameChat);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        logger.info("Chat Id afterconclosed " + session.getAttributes().get("chatId"));
        Integer chatId = (Integer) session.getAttributes().get("chatId");

//        TODO : how to ? : Users user = getCurrentUser();
//        MessageSocket messageSocket = new MessageSocket();
//        messageSocket.setType("quit");
//        messageSocket.setChat(chatId);
//        messageSocket.setUser(user.getFirstname()+" "+user.getLastname());
//        messageSocket.setMessage("a quitté le chat.");
//        messageSocket.setMsgtimestamp(Timestamp.valueOf(LocalDateTime.now()));
//        ObjectMapper mapper = new ObjectMapper();
//        broadcast(mapper.writeValueAsString(messageSocket), chatId);

        // Quand le client quitte, on retire sa session
        chatSessions.get(chatId).remove(session);
        logger.info("Déconnecté du " + this.nameChat);
    }

    public void broadcast(String message, Integer chatId) throws IOException {
        if (chatSessions.get(chatId)!=null) {
            for (WebSocketSession session : chatSessions.get(chatId)) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }
}
