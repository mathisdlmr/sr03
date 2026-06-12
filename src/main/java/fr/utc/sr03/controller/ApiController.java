package fr.utc.sr03.controller;

import fr.utc.sr03.model.*;
import fr.utc.sr03.security.JwtUtil;
import fr.utc.sr03.services.*;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Resource
    private PasswordResetTokenService passwordResetTokenService;
    @Resource
    private JakartaEmail jakartaEmail;

    // --------------------//
    // ----- Helpers ----- //
    // --------------------//

    // Récupère l'utilisateur authentifié via le JWT
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return user;
        }
        return null;
    }

    /**
     * Valide le mot de passe : on demande au moins 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre
     * Retourne null si valide, sinon renvoi un message d'erreur
     */
    private String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Le mot de passe doit contenir au moins une majuscule.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Le mot de passe doit contenir au moins une minuscule.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Le mot de passe doit contenir au moins un chiffre.";
        }
        return null;
    }

    // ----------------------------------------------//
    // ----- Auth endpoints (préfix /api/auth) ----- //
    // ----------------------------------------------//

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
        String accessToken = jwtUtil.generateAccessToken(user.getMail());
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
        String newAccessToken = jwtUtil.generateAccessToken(email);
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
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Users.UserDTO.from(user));
    }

    /**
     * POST /api/auth/forgot-password
     * Body : mail
     * Envoie un lien de réinitialisation de mot de passe par email
     */
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String mail) {
        Users user = userService.getUserByEmailAddress(mail);
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", "Si cette adresse est enregistrée, un lien de réinitialisation a été envoyé."));
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(user, token);
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        jakartaEmail.sendMail(mail, "Réinitialisation de mot de passe",
            "<p>Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe (valable 5 minutes) :</p>" + "<p><a href='" + resetLink + "'>" + resetLink + "</a></p>");
        return ResponseEntity.ok(Map.of("success", "Si cette adresse est enregistrée, un lien de réinitialisation a été envoyé."));
    }

    /**
     * POST /api/auth/reset-password
     * Body : token, password
     * Réinitialise le mot de passe de l'utilisateur
     */
    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String password) {
        if (!passwordResetTokenService.validatePasswordResetToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lien de réinitialisation invalide ou expiré."));
        }

        String pwdError = validatePassword(password);
        if (pwdError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", pwdError));
        }

        Users user = passwordResetTokenService.getUserByToken(token);
        userService.changePassword(user, password);
        passwordResetTokenService.deletePasswordResetToken(token);
        return ResponseEntity.ok(Map.of("success", "Mot de passe réinitialisé avec succès."));
    }

    // ------------------------------------------------//
    // ----- Avatar endpoints (préfix /api/auth) ----- //
    // ------------------------------------------------//

    /**
     * POST /api/auth/avatar
     * Multipart : fichier de max 1Mo
     * Endpoint pour uploader un avatar (max 1Mo) qui sera stocké en base64 en BDD
     */
    @PostMapping("/auth/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Aucun fichier fourni."));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image."));
        }
        if (file.getSize() > 1_048_576) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'image ne doit pas dépasser 1 Mo."));
        }

        try {
            String base64 = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
            user.setAvatar(base64);
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("success", true, "avatar", base64));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'upload de l'avatar."));
        }
    }

    /**
     * DELETE /api/auth/avatar
     * Supprime l'avatar de l'utilisateur connecté
     */
    @DeleteMapping("/auth/avatar")
    public ResponseEntity<?> deleteAvatar() {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        user.setAvatar(null);
        userService.saveUser(user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // -----------------------------------------------//
    // ----- Chat endpoints (préfix /api/chats) ----- //
    // -----------------------------------------------//

    /**
     * GET /api/chats/mine
     * Liste les salons dont l'utilisateur connecté est créateur
     */
    @GetMapping("/chats/mine")
    public ResponseEntity<List<Chat>> getMyChats() {
        Users user = getCurrentUser();
        if (user == null){
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(chatService.getChatByCreatorId(user.getId()));
    }

    /**
     * GET /api/chats/invited
     * Liste les salons auxquels l'utilisateur connecté est invité
     */
    @GetMapping("/chats/invited")
    public ResponseEntity<List<Chat>> getInvitedChats() {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(chatService.getChatsByInvitations(user.getId()));
    }

    /**
     * POST /api/chats
     * Crée un nouveau salon, le créateur est l'utilisateur connecté
     * Body JSON : { "title": "...", "description": "...", "startsAt":
     * "2026-06-10T14:00", "durationMinutes": 60 }
     */
    @PostMapping("/chats")
    public ResponseEntity<?> createChat(@RequestBody ChatDTO chatDTO) {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (chatDTO.getTitle() == null || chatDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le titre est obligatoire."));
        }
        Chat chat = new Chat();
        chat.setCreator(user);
        chat.setTitle(chatDTO.getTitle().trim());
        chat.setDescription(chatDTO.getDescription() != null ? chatDTO.getDescription().trim() : "");
        chat.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        if (chatDTO.getStartsAt() != null && !chatDTO.getStartsAt().isBlank()) { // Si on a une date de début -> chat de 1h par défaut (pour prévoir une réunion par exemple). Sinon, le chat est disponible immédiatement et pendant 10 jours par défaut
            try {
                LocalDateTime startsAt = LocalDateTime.parse(chatDTO.getStartsAt());
                int duration = chatDTO.getDurationMinutes() > 0 ? chatDTO.getDurationMinutes() : 60;
                chat.setEndsAt(Timestamp.valueOf(startsAt.plusMinutes(duration)));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Format de date invalide."));
            }
        } else {
            chat.setEndsAt(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
        }

        chatService.saveChat(chat);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * GET /api/chats/{id}
     * Recupère les informations d'un salon
     */
    @GetMapping("/chats/{id}")
    public ResponseEntity<Chat> getChat(@PathVariable int id) {
        Users user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();

        Chat chat = chatService.getChatById(id);
        if (chat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(chat);
    }

    /**
     * DELETE /api/chats/{id}
     * Supprime un salon si l'utilisateur connecté en est le créateur
     */
    @DeleteMapping("/chats/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable int id) {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(id);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        if (chat.getCreator().getId() != user.getId()) {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'êtes pas le créateur de ce salon."));
        }
        chatService.deleteChat(chat.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // -----------------------------------------------------------//
    // ----- Invitation endpoints (préfix /api/invitations) ----- //
    // -----------------------------------------------------------//

    /**
     * GET /api/invitations/{idChat}
     * Récupère la liste des utilisateurices invited au chat
     */
    @GetMapping("/invitations/{idChat}")
    public ResponseEntity<List<Invitation>> getInvitationsToChat(@PathVariable int idChat) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(invitationService.getInvitationByChatId(idChat));
    }

    /**
     * GET /api/invitations/users/{chatId}
     * Récupère la liste des utilisateurices invited au chat
     */
    @GetMapping("/invitations/users/{idChat}")
    public ResponseEntity<List<Users>> getInvitedUsersToChat(@PathVariable int idChat) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(invitationService.getInvitedUsersByChatId(idChat));
    }

    /**
     * POST /api/invitations
     * Invite un utilisateur dans un salon (le demandeur doit être le créateur).
     */
    @PostMapping("/invitations")
    public ResponseEntity<?> inviteUser(@RequestParam int idUser, @RequestParam int idChat) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        if (chat.getCreator().getId() != currentUser.getId()) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le créateur peut inviter des membres."));
        }
        if (idUser == currentUser.getId()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous ne pouvez pas vous inviter vous-même."));
        }

        Users invitedUser = userService.getUserById(idUser);
        if (invitedUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur introuvable."));
        }
        if (invitationService.isInvited(idChat, idUser)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cet utilisateur est déjà invité à ce salon."));
        }

        Invitation invitation = new Invitation();
        invitation.setChat(chat);
        invitation.setUser(invitedUser);
        invitation.setInvitationDate(Timestamp.valueOf(LocalDateTime.now()));
        invitationService.saveInvitation(invitation);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * DELETE /api/invitations/{userId}/{chatId}
     * Supprime l'invitation d'un utilisateur à un chat (le demandeur doit être le créateur)
     */
    @DeleteMapping("/invitations/{userId}/{chatId}")
    public ResponseEntity<?> deleteInvitationUser(@PathVariable int userId, @PathVariable int chatId) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        if (chat.getCreator().getId() != currentUser.getId()) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le créateur peut supprimer des membres."));
        }

        Users invitedUser = userService.getUserById(userId);
        if (invitedUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur introuvable."));
        }

        // On récupère l'invitation à supprimer
        Invitation invitation = invitationService.getInvitationByChatAndUserId(chat.getId(), invitedUser.getId());
        if (invitation == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'êtes pas invité de ce salon."));
        }

        invitationService.deleteInvitation(invitation.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * DELETE /api/invitations/{chatId}
     * Supprime l'invitation à un chat de l'utilisateur connecté
     */
    @DeleteMapping("/invitations/{chatId}")
    public ResponseEntity<?> deleteInvitation(@PathVariable int chatId) {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        Invitation invitation = invitationService.getInvitationByChatAndUserId(chat.getId(), user.getId());
        if (invitation == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'êtes pas invité de ce salon."));
        }

        invitationService.deleteInvitation(invitation.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ---------------------------//
    // ----- User endpoints ----- //
    // ---------------------------//

    /**
     * GET /api/users/{chatId}/search?q=...
     * Recherche d'utilisateurs par nom, prénom ou email (pour l'autocomplétion d'invitation par exemple)
     */
    @GetMapping("/users/{chatId}/search")
    public ResponseEntity<?> searchUninvitedUsers(@PathVariable int chatId, @RequestParam String q) {
        Users user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<Users> users = userService.searchUsers(q.trim());
        List<Users.UserDTO> dtos = users.stream()
            .filter(u -> u.getId() != user.getId())
                .filter(u -> !invitationService.isInvited(chatId, u.getId()))
            .map(Users.UserDTO::from)
            .toList();
        return ResponseEntity.ok(dtos);
    }
}
