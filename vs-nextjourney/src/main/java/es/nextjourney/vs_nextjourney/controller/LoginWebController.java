package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;
import java.util.Arrays;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LoginWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/sign_in") // quitar de webcontroler
    public String signIn(Principal principal) {
        if (principal != null) {
            return "redirect:/user_profile";
        }
        return "sign_in";
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
    public String newUser(User user, @RequestParam("imageFile") MultipartFile file) throws IOException, SQLException {
        user.setRoles(Arrays.asList("USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setImageFile(new javax.sql.rowset.serial.SerialBlob(file.getBytes()));
            image.setContentType(file.getContentType());

            user.setImage(image);
        }
        userService.saveUser(user);

        return "redirect:/sign_in";
    }

    @GetMapping("/user_profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }

        User user = userService.findByUserName(principal.getName());

        model.addAttribute("user", user);

        return "user_profile";
    }

    @GetMapping("/user_profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }

        User user = userService.findByUserName(principal.getName());

        model.addAttribute("user", user);

        return "edit_profile";
    }

    @PostMapping("/user_profile/edit")
    public String editProfile(User user, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }

        User user2 = userService.findByUserName(principal.getName());

        user2.setName(user.getName());
        user2.setLastName(user.getLastName());
        user2.setDateOfBirth(user.getDateOfBirth());
        user2.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user2.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.modifyUser(user2);

        return "redirect:/user_profile";
    }

}

// añadir loginerror