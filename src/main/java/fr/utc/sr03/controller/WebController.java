package fr.utc.sr03.controller;


import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @Resource
    private UserService userService;


    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/users")
    public String users(Model model) {
        model.addAttribute("myusers",userService.getAllUsers());
        return "users";
    }



}