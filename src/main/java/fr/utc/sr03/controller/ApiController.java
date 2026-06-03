package fr.utc.sr03.controller;

import fr.utc.sr03.model.*;
import fr.utc.sr03.security.JwtUtil;
import fr.utc.sr03.services.ChatService;
import fr.utc.sr03.services.InvitationService;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api")
public class ApiController {

    @Resource
    private UserService userService;
    @Resource
    private ChatService chatService;
    @Resource
    private InvitationService invitationService;
    @Resource
    private JwtUtil jwtUtil;

    // ----- Helpers ----- //
    // Récupère l'utilisateur authentifié via le JWT
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return user;
        }
        return null;
    }

    // ----- Auth endoints (préfix /api/auth) ----- //

    /**
     * POST /api/auth/login
     * Body (form-data ou JSON) : mail, password
     * Retourne : access_token (24h), refresh_token (7j), info de l'utilisateur connecté
    */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestParam String mail, @RequestParam String password) {
        Users user = userService.findByCredentials(mail, password);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Identifiants incorrects."));
        }
        if (!user.isActive()) {
            return ResponseEntity.status(403).body(Map.of("error", "Votre compte est désactivé."));
        }
        String accessToken  = jwtUtil.generateAccessToken(user.getMail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getMail());
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, Users.UserDTO.from(user)));
    }

    /**
     * POST /api/auth/refresh
     * Header : Authorization: Bearer <refresh_token>
     * Retourne : nouvel access_token + nouvel refresh_token
    */
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token manquant."));
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token) || !jwtUtil.isRefreshToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token invalide ou expiré."));
        }
        String email = jwtUtil.extractEmail(token);
        Users user = userService.getUserByEmailAddress(email);
        if (user == null || !user.isActive()) {
            return ResponseEntity.status(401).body(Map.of("error", "Utilisateur introuvable ou désactivé."));
        }
        String newAccessToken  = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, Users.UserDTO.from(user)));
    }

    /**
     * GET /api/auth/me
     * Retourne : les infos de l'utilisateur connecté (extrait du JWT)
    */
    @GetMapping("/auth/me")
    public ResponseEntity<?> me() {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Users.UserDTO.from(user));
    }

    // ----- Chat endpoints (préfix /api/chats) ----- //

    /**
     * GET /api/chats/mine
     * Liste les salons dont l'utilisateur connecté est créateur
    */
    @GetMapping("/chats/mine")
    public ResponseEntity<List<Chat>> getMyChats() {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(chatService.getChatByCreatorId(user.getId()));
    }

    /**
     * GET /api/chats/invited
     * Liste les salons auxquels l'utilisateur connecté est invité
    */
    @GetMapping("/chats/invited")
    public ResponseEntity<List<Chat>> getInvitedChats() {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(chatService.getChatsByInvitations(user.getId()));
    }

    /**
     * POST /api/chats
     * Crée un nouveau salon, le créateur est l'utilisateur connecté
     * Body JSON : { "title": "...", "description": "..." }
    */
    @PostMapping("/chats")
    public ResponseEntity<?> createChat(@RequestBody ChatDTO chatDTO) {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();

        Chat chat = new Chat();
        chat.setCreator(user);
        chat.setTitle(chatDTO.getTitle());
        chat.setDescription(chatDTO.getDescription());
        chat.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        // Date de fin : +10 jours par défaut (à améliorer)
        chat.setEndsAt(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
        chatService.saveChat(chat);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * DELETE /api/chats/{id}
     * Supprime un salon si l'utilisateur connecté en est le créateur
    */
    @DeleteMapping("/chats/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable int id) {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();

        Chat chat = chatService.getChatById(id);
        if (chat == null) return ResponseEntity.notFound().build();
        if (chat.getCreator().getId() != user.getId()) {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'êtes pas le créateur de ce salon."));
        }
        chatService.deleteChat(chat.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ----- Invitation endpoints (préfix /api/invitations) ----- //

    /**
     * POST /api/invitations
     * Invite un utilisateur dans un salon (le demandeur doit être le créateur).
    */
    @PostMapping("/invitations")
    public ResponseEntity<?> inviteUser(@RequestParam int idUser, @RequestParam int idChat) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        Chat chat = chatService.getChatById(idChat);
        if (chat == null) return ResponseEntity.notFound().build();
        if (chat.getCreator().getId() != currentUser.getId()) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le créateur peut inviter des membres."));
        }

        Users invitedUser = userService.getUserById(idUser);
        if (invitedUser == null) return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur introuvable."));

        Invitation invitation = new Invitation();
        invitation.setChat(chat);
        invitation.setUser(invitedUser);
        invitation.setInvitationDate(Timestamp.valueOf(LocalDateTime.now()));
        invitationService.saveInvitation(invitation);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
