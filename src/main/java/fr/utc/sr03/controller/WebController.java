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

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }

    @GetMapping("/login")
    public String loginForm() {
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
        } else if (!user.isAdmin()) {
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

    @GetMapping("/reset_pwd")
    public String resetPasswordForm() {
        return "reset_pwd";
    }

    @PostMapping("/reset_pwd")
    public String resetPasswordSubmit(Model model, @RequestParam String mail) {
        Users user = userService.getUserByEmailAddress(mail);
        if (user == null) {
            model.addAttribute("error", "Cette adresse mail n'appartient à aucun.e utilisateur.ice");
            return "reset_pwd";
        }

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);

        JakartaEmail jakartaEmail = new JakartaEmail();
        jakartaEmail.sendMail(user.getMail(), "Mot de passe oublié", token);

        model.addAttribute("success", "Un mail vous a été envoyé pour réinitialiser le mot de passe !");
        return "reset_pwd";
    }

    @GetMapping("/")
    public String admin(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (!user.isAdmin()) {
            return "unauthorized";
        }

        // TODO : ici faut faire de la pagination sur les users et chats ...

        return "admin";
    }
}