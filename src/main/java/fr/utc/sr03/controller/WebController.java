package fr.utc.sr03.controller;

import fr.utc.sr03.model.Chat;
import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.ChatService;
import fr.utc.sr03.services.InvitationService;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class    WebController {

    @Resource
    private UserService userService;

    @Resource
    private ChatService chatService;

    @Resource
    private InvitationService invitationService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(HttpSession session) {
        // TODO : logique de vérification des identificants par rapports aux params de la requete
        // On récupère ensuite les infos du user dans la variable user pour la définir en session
        session.setAttribute("user", user);
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Chat> ownedChats   = chatService.getChatByCreatorId(user.getId());
        List<Chat> invitedChats = chatService.getChatsByInvitation(user.getId()); // TODO : j'ai la flemme pour l'instant de faire celle là

        model.addAttribute("user", user);
        model.addAttribute("ownedChats", ownedChats);
        model.addAttribute("invitedChats", invitedChats);

        return "index";
    }

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model, @RequestParam(value = "chat_id") int chatId) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        boolean isOwner  = chatService.isOwner(chatId, user.getId());
        boolean isInvited = invitationService.isInvited(chatId, user.getId());
        if (!isOwner && !isInvited) {
            return "unauthorized";
        }

        Chat chat = chatService.getChatById(chatId);
        model.addAttribute("user", user);
        model.addAttribute("chat", chat);
        return "chat";
    }

    @GetMapping("/admin")
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

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }
}