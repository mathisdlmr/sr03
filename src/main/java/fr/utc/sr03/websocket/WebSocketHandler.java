package fr.utc.sr03.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WebSocketHandler extends TextWebSocketHandler {

    private final String nameChat;
    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    private final List<WebSocketSession> sessions;
    private final List<MessageSocket> messageSocketsHistory;

    public WebSocketHandler(String nameChat) {
        this.nameChat = nameChat;
        this.messageSocketsHistory = new ArrayList<>();
        this.sessions = new ArrayList<>();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String receivedMessage = (String) message.getPayload();
        MessageSocket messageSocket = mapper.readValue(receivedMessage, MessageSocket.class);

        //Pour stocker le message dans l'historique
        messageSocketsHistory.add(messageSocket);

        //Envoi du message à tous les connectés
        this.broadcast(messageSocket.getUser()+ " : " + messageSocket.getMessage());

    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        //On stocke la session du client dans une liste
        sessions.add(session);
        logger.info(session.getId());

        //J'affiche l'historique du salon
        for(MessageSocket messageSocket : messageSocketsHistory){
            session.sendMessage(new TextMessage(messageSocket.getUser()+ " : " + messageSocket.getMessage()));
        }

        logger.info("Connecté sur le " + this.nameChat);

    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        //Quand le client quitte, on retire sa session
        sessions.remove(session);
        logger.info("Déconnecté du " + this.nameChat);

    }

    public void broadcast(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
