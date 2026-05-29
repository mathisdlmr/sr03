package fr.utc.sr03.controller;

import fr.utc.sr03.model.Chat;
import fr.utc.sr03.model.ChatDTO;
import fr.utc.sr03.model.Invitation;
import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.ChatService;
import fr.utc.sr03.services.InvitationService;
import fr.utc.sr03.services.JakartaEmail;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.hibernate.grammars.hql.HqlParser;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class ApiController {

    @Resource
    private UserService userService;
    @Resource
    private ChatService chatService;
    @Resource
    private InvitationService invitationService;


//    @PostMapping(value = "/create")
//    public void create() {
//        Users user = new Users();
//        user.setFirstname("Cédric");
//        user.setLastname("Martinet");
//        user.setMail("cedric.martinet@utc.fr");
//        userService.saveUser(user);
//    }

    @GetMapping(value = "/liste")
    public List<Users> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/oneUser/{id}")
    public Users getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @GetMapping(value = "/testmail")
    public void testmail() {
        // Test envoi Mail
        JakartaEmail jakartaEmail = new JakartaEmail();
        jakartaEmail.sendMail("test.name@mail.com", "Subject", "Content");
    }

    // --- User ---
    // login
    @PostMapping("/loginuser")
    public Users loginUserSubmit(HttpSession session, @RequestParam String mail, @RequestParam String password) {
        Users user = userService.findByCredentials(mail, password);
        if (user == null) {
            //model.addAttribute("error", "Identifiants incorrects.");
            return null; // ou codes pour def type d'erreur
        }
        if (!user.isActive()) {
            //model.addAttribute("error", "Votre compte est désactivé.");
            return null;
        }
        session.setAttribute("user", user);
        return user;
    }

    // logout ?
    @PostMapping("/logout")
    public Boolean logout(HttpSession session) {
        session.invalidate();
        return true;
    }

    // sign in (on n'utilise plus l'envoit des mails)

    // edit profile


    // create chat
    @PostMapping(value = "/createchat")
    public boolean createChat(@RequestBody ChatDTO chatDTO) {
        // to test in postman : Body->raw JSON
        //System.out.println(chatDTO.getTitle());
        //System.out.println(chatDTO.getDescription());
        //System.out.println(chatDTO.getCreatorId());

        Chat chat = new Chat();

        Users user = userService.getUserById(chatDTO.getCreatorId());
        if (user == null) {
            System.out.println("no user");
            return false;
        }
        chat.setCreator(user);

        chat.setTitle(chatDTO.getTitle());
        chat.setDescription(chatDTO.getDescription());

        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now());
        //System.out.println("Timestamp today : " + startDate);
        chat.setCreatedAt(startDate);

        // ToDo : define end date (-> timestamp) or smtg in requestbody
        Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(10));
        //System.out.println("Timestamp in 10 days : " + endDate);
        chat.setEndsAt(endDate);

        chatService.saveChat(chat);
        return true;
    }

    // edit chat
    @PostMapping(value = "/editchat/{idChat}")
    public boolean editChat(@RequestParam int idUser, @PathVariable int idChat, @RequestParam String title, @RequestParam String description, @RequestParam(required = false) Timestamp endDate) {
        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            System.out.println("no chat");
            return false;
        }
        if (chat.getCreator().getId() != idUser) {
            System.out.println("not the owner of the chat");
            return false;
        }
        chat.setTitle(title);
        chat.setDescription(description);
        if (endDate != null) { // ex endate ok : 2027-10-03 16:15:06
            // System.out.println("EndDate : " + endDate); // 2027-10-03 16:15:06.0
            chat.setEndsAt(endDate);
        }
        chatService.saveChat(chat);
        return true;
    }

    // delete chat
    @PostMapping(value = "/deletechat/{idChat}")
    public boolean deleteChat(@RequestParam int idUser, @PathVariable int idChat) {
        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            System.out.println("no chat");
            return false;
        }
        if (chat.getCreator().getId() != idUser) {
            System.out.println("not the owner of the chat");
            return false;
        }

        chatService.deleteChat(chat.getId());
        return true;
    }

    // invite user to chat
    @PostMapping(value = "/inviteuserchat")
    public boolean inviteUserChat(@RequestParam int idUser, @RequestParam int idChat) {
        Invitation invitation = new Invitation();
        Chat chat = chatService.getChatById(idChat);
        if (chat == null) {
            System.out.println("no chat");
            return false;
        }
        Users user = userService.getUserById(idUser);
        if (user == null) {
            System.out.println("no user");
            return false;
        }
        invitation.setChat(chat);
        invitation.setUser(user);
        invitation.setInvitationDate(Timestamp.valueOf(LocalDateTime.now()));
        invitationService.saveInvitation(invitation);

        // need to update sets in Users & Chat ?

        return true;
    }

    // list user's chats (owner)
    @GetMapping(value = "/listechatsowned/{id}")
    public List<Chat> getOwnedChats(@PathVariable int id) {
        return chatService.getChatByCreatorId(id);
    }

    // list user's chats (invited)
    @GetMapping(value = "/listechatsinvited/{id}")
    public List<Chat> getInvitedChats(@PathVariable int id) {
        return chatService.getChatsByInvitations(id);
    }

    // chat window
        // send message in chat -> websocket
        // list users connected to the chat
        // keep timestamp of each message on screen ("x minutes ago") -> to do in frontend
}
