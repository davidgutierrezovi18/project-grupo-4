package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.Arrays;

@Controller
public class LoginWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/sign_in") // quitar de webcontroler
    public String signIn() {
        return "sign_in";
    }

    @PostMapping("/login")
    public String login(String username, String password, HttpSession session) {

        try {
            User user = userService.findByUserName(username);

            boolean loginOk = false;
            String storedPassword = user.getPassword();

            if (storedPassword != null && storedPassword.startsWith("$2")) {
                loginOk = passwordEncoder.matches(password, storedPassword);
            } else {
                
                loginOk = storedPassword != null && storedPassword.equals(password);
                if (loginOk) {
                    user.setPassword(passwordEncoder.encode(password));
                    userService.modifyUser(user);
                }
            }

            if (loginOk) {
                session.setAttribute("user", user);
                return "redirect:/user_profile";
            }

        } catch (Exception e) {
        }

        return "redirect:/loginerror";
    }

    @GetMapping("/loginerror") // hacer pagina loginerror
    public String loginerror() {
        return "login_error";
    }

    @GetMapping("/register") // quitar de webcontroler
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String newUser(User user) {
        user.setRoles(Arrays.asList("USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);

        return "redirect:/sign_in";
    }

    @GetMapping("/user_profile")
    public String profile(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user"); //cambiar a principal

        if (user == null) {
            return "redirect:/sign_in";
        }

        model.addAttribute("user", user);

        return "user_profile";
    }

    @GetMapping("/user_profile/edit")
    public String editProfile(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user"); //cambiar a principal

        if (user == null) {
            return "redirect:/sign_in";
        }

        model.addAttribute("user", user);

        return "edit_profile";
    }

    @PostMapping("/user_profile/edit")
    public String editProfile(User user, HttpSession session) {

        User user2 = (User) session.getAttribute("user"); //cambiar a principal

        if (user2 == null) {
            return "redirect:/sign_in";
        }

        user2.setName(user.getName());
        user2.setLastName(user.getLastName());
        user2.setDateOfBirth(user.getDateOfBirth());
        user2.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user2.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.modifyUser(user2);

        session.setAttribute("user", user2);

        return "redirect:/user_profile";
    }

}

// añadir loginerror