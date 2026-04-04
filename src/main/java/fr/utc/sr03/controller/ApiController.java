package fr.utc.sr03.controller;


import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.JakartaEmail;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class ApiController {


    @Resource
    private UserService userService;


    @PostMapping(value = "/create")
    public void create(){
        Users user = new Users();
        user.setFirstname("Cédric");
        user.setLastname("Martinet");
        user.setMail("cedric.martinet@utc.fr");
        userService.saveUser(user);
    }

    @GetMapping(value = "/liste")
    public List<Users> getUsers(){
        return userService.getAllUsers();
    }

    @GetMapping(value = "/oneUser/{id}")
    public Users getUserById(@PathVariable int id){
        return userService.getUserById(id);
    }

    @GetMapping(value = "/testmail")
    public void testmail(){

        //Test envoi Mail
        JakartaEmail jakartaEmail = new JakartaEmail();
        jakartaEmail.sendMail();
    }



}
