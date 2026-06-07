package fr.utc.sr03.websocket;

import fr.utc.sr03.security.JwtUtil;
import fr.utc.sr03.services.ChatService;
import fr.utc.sr03.services.InvitationService;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserService userService;
    @Resource
    private ChatService chatService;
    @Resource
    private InvitationService invitationService;

    @Value("${spring.websocket.max-text-message-size:5242880}")
    private int maxTextMessageSize;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
            new WebSocketHandler(jwtUtil, userService, chatService, invitationService), "/ws/chat/*")
            .setAllowedOrigins("*")
            .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
