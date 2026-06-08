package fr.utc.sr03.controller;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.JakartaEmail;
import fr.utc.sr03.services.PasswordResetTokenService;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

import java.util.UUID;

@Controller
public class WebController {

    @Resource
    private UserService userService;

    @Resource
    private PasswordResetTokenService passwordResetTokenService;

    @Resource
    private JakartaEmail jakartaEmail;

    // --------------------//
    // ----- Helpers ----- //
    // --------------------//

    /**
     * Vérifie que l'utilisateur connecté est admin
     * On se base sur l'email pour vérifier chaque fois sur la dernière version de la BDD
     * et non simplement sur la session
     */
    private String requireAdmin(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Users user = userService.getUserByEmailAddress(email);
        if (user == null || !user.isActive()) {
            session.invalidate();
            return "redirect:/login";
        }
        if (!user.isAdmin()) {
            return "unauthorized";
        }
        model.addAttribute("sessionUser", user);
        return null;
    }

    // On surcharge pour les endpoints qui n'ont pas de Model (les toggle, delete...)
    private String requireAdmin(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Users user = userService.getUserByEmailAddress(email);
        if (user == null || !user.isActive()) {
            session.invalidate();
            return "redirect:/login";
        }
        if (!user.isAdmin()) {
            return "unauthorized";
        }
        return null;
    }

    // Génère une URL de base dynamiquement (dans le cas du TD, http://localhost:8080)
    private String getBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
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

    // On définit un sanitizer basique pour éviter que le user puisse
    // rediriger vers un site externe après une requête POST
    private String sanitizeReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return "/listuser";
        }
        if (!returnUrl.startsWith("/") || returnUrl.contains("://") || returnUrl.startsWith("//")) {
            return "/listuser";
        }
        return returnUrl;
    }

    // Fonction récupérée et adaptée du diapo "TD3" sur Moodle
    private static String generatePassword(int length) {
        String upper = "ABCDEFGHIJKLMNOP";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "1234567890";
        String special = "!@#$%^&*()";
        String all = upper + lower + digits + special;

        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt((int) (Math.random() * upper.length())));
        sb.append(lower.charAt((int) (Math.random() * lower.length())));
        sb.append(digits.charAt((int) (Math.random() * digits.length())));
        for (int i = 3; i < length; i++) {
            sb.append(all.charAt((int) (Math.random() * all.length())));
        }
        return sb.toString();
    }

    // -----------------//
    // ----- Auth ----- //
    // -----------------//

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("userEmail") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(HttpSession session, Model model, @RequestParam String mail, @RequestParam String password) {
        Users user = userService.findByCredentials(mail, password);
        if (user == null) {
            model.addAttribute("error", "Identifiants incorrects.");
            return "login";
        }
        if (!user.isActive()) {
            model.addAttribute("error", "Votre compte est désactivé.");
            return "login";
        }
        if (!user.isAdmin()) {
            return "unauthorized";
        }
        session.setAttribute("userEmail", user.getMail());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ---------------------------//
    // ----- Reset Password ----- //
    // ---------------------------//

    @GetMapping("/askresetpwd")
    public String askResetPasswordForm() {
        return "ask_reset_pwd";
    }

    @PostMapping("/askresetpwd")
    public String askResetPasswordSubmit(HttpServletRequest request, Model model, @RequestParam String mail) {
        Users user = userService.getUserByEmailAddress(mail);
        if (user == null) {
            model.addAttribute("error", "Cette adresse mail n'est pas enregistrée.");
            return "ask_reset_pwd";
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(user, token);
        String resetLink = getBaseUrl(request) + "/resetpwd?token=" + token;
        jakartaEmail.sendMail(mail, "Réinitialisation de mot de passe",
            "<p>Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe (valable 5 minutes) :</p>"
            + "<p><a href='" + resetLink + "'>" + resetLink + "</a></p>");
        model.addAttribute("success", "Un lien de réinitialisation a été envoyé à votre adresse mail.");
        return "ask_reset_pwd";
    }

    @GetMapping("/resetpwd")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        if (!passwordResetTokenService.validatePasswordResetToken(token)) {
            model.addAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "ask_reset_pwd";
        }
        Users user = passwordResetTokenService.getUserByToken(token);
        model.addAttribute("token", token);
        model.addAttribute("email", user.getMail());
        return "reset_pwd";
    }

    @PostMapping("/resetpwd")
    public String resetPasswordSubmit(@RequestParam String token, @RequestParam String password, Model model) {
        if (!passwordResetTokenService.validatePasswordResetToken(token)) {
            model.addAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "ask_reset_pwd";
        }

        String pwdError = validatePassword(password);
        if (pwdError != null) {
            model.addAttribute("error", pwdError);
            model.addAttribute("token", token);
            Users user = passwordResetTokenService.getUserByToken(token);
            model.addAttribute("email", user.getMail());
            return "reset_pwd";
        }

        Users user = passwordResetTokenService.getUserByToken(token);
        userService.changePassword(user, password);
        passwordResetTokenService.deletePasswordResetToken(token);
        return "redirect:/login";
    }

    // -------------------------//
    // ----- (Admin) Home ----- //
    // -------------------------//

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        return "redirect:/listuser";
    }

    // ----------------------------//
    // ----- User Management ----- //
    // ----------------------------//

    @GetMapping("/listuser")
    public String listUsers(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String search) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        Page<Users> usersPage = userService.getUsers(true, search, page, 10);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("inactive", false);
        model.addAttribute("listUrl", "/listuser");
        return "users_list";
    }

    @GetMapping("/listinactiveuser")
    public String listInactiveUsers(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String search) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        Page<Users> usersPage = userService.getUsers(false, search, page, 10);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("inactive", true);
        model.addAttribute("listUrl", "/listinactiveuser");
        return "users_list";
    }

    @GetMapping("/create")
    public String createUserForm(HttpSession session, Model model) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        return "create_user";
    }

    @PostMapping("/create")
    public String createUserSubmit(HttpSession session, HttpServletRequest request, Model model, @RequestParam String firstname, @RequestParam String lastname, @RequestParam String mail, @RequestParam(defaultValue = "false") boolean admin) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }

        if (userService.getUserByEmailAddress(mail) != null) {
            model.addAttribute("error", "Cette adresse mail est déjà utilisée.");
            return "create_user";
        }

        String tempPassword = generatePassword(10);
        Users user = new Users();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setMail(mail);
        user.setPassword(BCrypt.hashpw(tempPassword, BCrypt.gensalt()));
        user.setAdmin(admin);
        user.setActive(true);
        userService.saveUser(user);
        String safeFirstname = HtmlUtils.htmlEscape(firstname); // Comme le firstName est définir par le user, on l'échappe pour éviter des XSS
        String loginUrl = getBaseUrl(request) + "/login";

        jakartaEmail.sendMail(mail, "Bienvenue sur SR03 Chat",
            "<p>Bonjour " + safeFirstname + ",</p>"
            + "<p>Votre compte a été créé. Voici votre mot de passe temporaire : <strong>" + tempPassword + "</strong></p>"
            + "<p>Connectez-vous sur <a href='" + loginUrl + "'>" + loginUrl + "</a></p>");

        model.addAttribute("success", "Utilisateur créé avec succès. Un mail de confirmation a été envoyé.");
        return "create_user";
    }

    @GetMapping("/edituser/{id}")
    public String editUserForm(HttpSession session, Model model, @PathVariable int id) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        Users user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/listuser";
        }
        model.addAttribute("editedUser", user);
        return "edit_user";
    }

    @PostMapping("/edituser/{id}")
    public String editUserSubmit(HttpSession session, Model model, @PathVariable int id, @RequestParam String firstname, @RequestParam String lastname, @RequestParam String mail, @RequestParam(defaultValue = "false") boolean admin, @RequestParam(required = false) String password) {
        String check = requireAdmin(session, model);
        if (check != null) {
            return check;
        }
        Users user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/listuser";
        }

        if (password != null && !password.isBlank()) {
            String pwdError = validatePassword(password);
            if (pwdError != null) {
                model.addAttribute("error", pwdError);
                model.addAttribute("editedUser", user);
                return "edit_user";
            }
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setMail(mail);
        user.setAdmin(admin);
        userService.saveUser(user);
        return "redirect:/listuser";
    }

    @PostMapping("/toggleuser/{id}")
    public String toggleUser(HttpSession session, @PathVariable int id, @RequestParam(defaultValue = "/listuser") String returnUrl) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        Users user = userService.getUserById(id);
        if (user != null) {
            user.setActive(!user.isActive());
            userService.saveUser(user);
        }
        return "redirect:" + sanitizeReturnUrl(returnUrl);
    }

    @PostMapping("/deleteuser/{id}")
    public String deleteUser(HttpSession session, @PathVariable int id, @RequestParam(defaultValue = "/listuser") String returnUrl) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        userService.deleteUser(id);
        return "redirect:" + sanitizeReturnUrl(returnUrl);
    }
}
