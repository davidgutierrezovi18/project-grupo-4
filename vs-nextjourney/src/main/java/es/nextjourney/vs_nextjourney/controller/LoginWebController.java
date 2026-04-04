package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;
import java.util.Arrays;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LoginWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    //Sign-in
    @GetMapping("/sign_in") 
    public String signIn(Principal principal) {
        if (principal != null) {
            return "redirect:/user_profile";
        }
        return "sign_in";
    }

    //Login error
    @GetMapping("/loginerror")
    public String loginerror() {
        return "login_error";
    }

    //Register (create account) - GET
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    //Register (create account) - POST
    @PostMapping("/register")
    public String newUser(User user, @RequestParam("imageFile") MultipartFile file, Model model)
            throws IOException, SQLException {

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "register";
        }
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "El correo electrónico ya está en uso");
            return "register";
        }
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

    // User profile
    @GetMapping("/user_profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }

        User user = userService.findByUserName(principal.getName());

        model.addAttribute("user", user);

        return "user_profile";
    }

    // Edit profile - GET
    @GetMapping("/edit_profile")
    public String editProfile(Model model, Principal principal) {
        if (principal == null)
            return "redirect:/sign_in";
        User user = userService.findByUserName(principal.getName());
        model.addAttribute("user", user);
        return "edit_profile";
    }

    // Edit profile - POST
    @PostMapping("/edit_profile")
    public String editProfile(
            User user,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam("imageFile") MultipartFile file,
            Principal principal,
            Model model) throws IOException, SQLException {

        if (principal == null) {
            return "redirect:/sign_in";
        }

        User user2 = userService.findByUserName(principal.getName());

        if (!user2.getUsername().equals(user.getUsername()) && userService.usernameExists(user.getUsername())) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "Ese nombre de usuario ya existe");
            return "edit_profile";
        }
        if (!user2.getEmail().equals(user.getEmail()) && userService.emailExists(user.getEmail())) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "Ese correo electrónico ya está en uso");
            return "edit_profile";
        }

        user2.setName(user.getName());
        user2.setLastName(user.getLastName());
        user2.setDateOfBirth(user.getDateOfBirth());
        user2.setEmail(user.getEmail());
        user2.setUsername(user.getUsername());

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setImageFile(new javax.sql.rowset.serial.SerialBlob(file.getBytes()));
            image.setContentType(file.getContentType());
            user2.setImage(image);
        }

        if (currentPassword != null && !currentPassword.isBlank()) {

            if (!passwordEncoder.matches(currentPassword, user2.getPassword())) {
                model.addAttribute("user", user2);
                model.addAttribute("error", "Contraseña incorrecta");
                return "edit_profile";
            }

            if (newPassword == null || newPassword.isBlank()) {
                model.addAttribute("user", user2);
                model.addAttribute("error", "Debes introducir una nueva contraseña");
                return "edit_profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("user", user2);
                model.addAttribute("error", "Las contraseñas no coinciden");
                return "edit_profile";
            }

            user2.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.modifyUser(user2);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user2.getUsername());
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails.getPassword(),
            userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/user_profile";
    }

    @PostMapping("/delete_profile")
    public String deleteProfile(Principal principal, HttpServletRequest request) throws ServletException {
    if (principal != null) {
        User user = userService.findByUserName(principal.getName());
        userService.deleteById(user.getId());
        request.logout(); 
    }
    return "redirect:/"; 
    }
}
