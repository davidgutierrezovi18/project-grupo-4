package es.nextjourney.vs_nextjourney.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginWebController {


    @GetMapping("/login")
    public String login() {
        return "sign_in"; 
    }

    @GetMapping("/loginerror")
    public String loginerror() {
        return "login_error"; // Lo tenemos que crear !!!
    }

}