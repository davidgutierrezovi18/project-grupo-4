package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.UserService;
import java.util.Optional;




@Controller
public class LoginWebController {

     @Autowired
    private UserService userService;


    @GetMapping("/sign_in") //quitar de webcontroler
	public String signIn() {
		return "sign_in";
	}

    @GetMapping("/loginerror") //hacer pagina loginerror
    public String loginerror() {
        return "login_error"; 
    }

    
    @GetMapping("/register") //quitar de webcontroler
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String newUser(User user) {
        user.setRol("USER");
        userService.saveUser(user);

        return "redirect:/sign_in";
    }

    @GetMapping("/user_profile")
    public String profile(Model model, Principal principal) {

        User user = userService.findByUserName(principal.getName());
        model.addAttribute("user", user);

        return "user_profile";
    }

    @GetMapping("/user_profile/edit")
    public String editProfile(Model model, Principal principal) {

        User user = userService.findByUserName(principal.getName());
        model.addAttribute("user", user);

        return "edit_profile";
    }

        @PostMapping("/user_profile/edit")
            public String editProfile(User user, Principal principal) {
            User user2 = userService.findByUserName(principal.getName());
            user2.setName(user.getName());
            user2.setLastName(user.getLastName());
            user2.setDateOfBirth(user.getDateOfBirth());
            user2.setEmail(user.getEmail());
            user2.setPassword(user.getPassword());
            userService.modifyUser(user2);
    
            return "redirect:/user_profile";
            }
        

    }



//añadir loginerror