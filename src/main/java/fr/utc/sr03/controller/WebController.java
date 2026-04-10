package fr.utc.sr03.controller;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class    WebController {

    @Resource
    private UserService userService;

    @RequestMapping(value = "/index")
    public String index(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if(!user.getId()){
            return "login";
        }

        // TODO : affiche tous les chats dont l’utilisateur est invité ou il est le propriétaire

        // TODO : un cadre sur la droite avec afficher la liste (items) des chats dont l’utilisateur est le propriétaire.
        // Chaque item a un lien qui pointe vers la page du chat désigné et des liens pour modifier ou supprimer le chat

        // TODO : Une icone en haut à gauche pour ouvrir une pop-up avec la liste (items) des chats dont l’utilisateur est un invité.
        //C haque item a un lien qui pointe vers la page du chat désigné

        return "index";
    }

    @RequestMapping(value = "/login")
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/admin")
    public String admin(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if(!user.isAdmin()){
            return "unauthorized";
        }

        // TODO : Lorsque l’utilisateur clique sur un lien chat une nouvelle fenêtre est ouverte. Elle est composé d’
        // un fil de discussion (TEXTAREA, ….) : une suite de messages consécutifs sur le même chat, classés de manière arborescente.
        // un formulaire pour éditer et poster un nouveau message sur le chat : un champ de texte avec un bouton pour envoyer le message
        // une liste d’utilisateurs connectés

        return "admin";
    }

    @RequestMapping(value = "/chat")
    public String chat(@RequestParam(value = "chat_id") int chat_id) {

        // TODO : Vérifier que le.a user a accès à ce chat

        // TODO : toute la logique métier pour récup les données

        return "chat";
    }

    // TODO : remove later
    @RequestMapping(value = "/users")
    public String users(Model model) {
        model.addAttribute("myusers", userService.getAllUsers());
        return "users";
    }
}