package fr.utc.sr03.controller;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.JakartaEmail;
import fr.utc.sr03.services.PasswordResetTokenService;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class WebController {

    @Resource
    private UserService userService;

    @Resource
    private PasswordResetTokenService passwordResetTokenService;

    // --- Helper ---

    private String requireAdmin(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (!user.isAdmin()) return "unauthorized";
        return null;
    }

    // Fonction récupérée et adaptée du diapo "TD3" sur Moodle
    private static String generatePassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOP1234567890!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    // --- Auth ---

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(HttpSession session, Model model,  @RequestParam String mail,  @RequestParam String password) {
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
        session.setAttribute("user", user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- Reset Password ---

    @GetMapping("/askresetpwd")
    public String askResetPasswordForm() {
        return "ask_reset_pwd";
    }

    @PostMapping("/askresetpwd")
    public String askResetPasswordSubmit(Model model, @RequestParam String mail) {
        Users user = userService.getUserByEmailAddress(mail);
        if (user == null) {
            model.addAttribute("error", "Cette adresse mail n'est pas enregistrée.");
            return "ask_reset_pwd";
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(user, token);
        String resetLink = "http://localhost:8080/resetpwd?token=" + token; // TODO : mettre une URL automatique (pas juste localhost:8080 hard-codé)
        new JakartaEmail().sendMail(mail, "Réinitialisation de mot de passe",
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

//    @GetMapping("/resetpwd_test")
//    public String resetPasswordFormTest(Model model) {
//        model.addAttribute("email", "my@email");
//        return "reset_pwd";
//    } // Test vue html, to delete

    @PostMapping("/resetpwd")
    public String resetPasswordSubmit(@RequestParam String token,  @RequestParam String password,  Model model) {
        if (!passwordResetTokenService.validatePasswordResetToken(token)) {
            model.addAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "ask_reset_pwd";
        }
        Users user = passwordResetTokenService.getUserByToken(token);
        userService.changePassword(user, password);
        passwordResetTokenService.deletePasswordResetToken(token);
        return "redirect:/login";
    }

    // --- (Admin) Home) ---

    @GetMapping("/")
    public String home(HttpSession session) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        return "redirect:/listuser";
    }

    // --- User Management ---
    
    @GetMapping("/listuser")
    public String listUsers(HttpSession session, Model model,  @RequestParam(defaultValue = "0") int page,  @RequestParam(defaultValue = "") String search) {
        String check = requireAdmin(session);
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
    public String listInactiveUsers(HttpSession session, Model model,  @RequestParam(defaultValue = "0") int page,  @RequestParam(defaultValue = "") String search) {
        String check = requireAdmin(session);
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
    public String createUserForm(HttpSession session) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        return "create_user";
    }

    @PostMapping("/create")
    public String createUserSubmit(HttpSession session, Model model,  @RequestParam String firstname, @RequestParam String lastname,  @RequestParam String mail,  @RequestParam(defaultValue = "false") boolean admin) {
        String check = requireAdmin(session);
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

        new JakartaEmail().sendMail(mail, "Bienvenue sur SR03 Chat",
            "<p>Bonjour " + firstname + ",</p>"
            + "<p>Votre compte a été créé. Voici votre mot de passe temporaire : <strong>" + tempPassword + "</strong></p>"
            + "<p>Connectez-vous sur <a href='http://localhost:8080/login'>http://localhost:8080/login</a></p>"); // TODO : mettre une URL automatique (pas juste localhost:8080 hard-codé)

        model.addAttribute("success", "Utilisateur créé avec succès. Un mail de confirmation a été envoyé.");
        return "create_user";
    }

    @GetMapping("/edituser/{id}")
    public String editUserForm(HttpSession session, Model model, @PathVariable int id) {
        String check = requireAdmin(session);
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
    public String editUserSubmit(HttpSession session, Model model,  @PathVariable int id, @RequestParam String firstname,  @RequestParam String lastname,  @RequestParam String mail,  @RequestParam(defaultValue = "false") boolean admin,  @RequestParam(required = false) String password) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        Users user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/listuser";
        }
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setMail(mail);
        user.setAdmin(admin);
        if (password != null && !password.isBlank()) {
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
        userService.saveUser(user);
        return "redirect:/listuser";
    }

    @PostMapping("/toggleuser/{id}")
    public String toggleUser(HttpSession session, @PathVariable int id,  @RequestParam(defaultValue = "/listuser") String returnUrl) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        Users user = userService.getUserById(id);
        if (user != null) {
            user.setActive(!user.isActive());
            userService.saveUser(user);
        }
        return "redirect:" + returnUrl;
    }

    @PostMapping("/deleteuser/{id}")
    public String deleteUser(HttpSession session, @PathVariable int id, @RequestParam(defaultValue = "/listuser") String returnUrl) {
        String check = requireAdmin(session);
        if (check != null) {
            return check;
        }
        userService.deleteUser(id);
        return "redirect:" + returnUrl;
    }
}
