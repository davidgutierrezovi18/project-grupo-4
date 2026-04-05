package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;
import java.util.Arrays;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import jakarta.validation.Valid;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LoginWebController {

    private static final Pattern PASSWORD_POLICY = Pattern
            .compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$");

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
    public String newUser(@Valid User user, BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile file, Model model)
            throws IOException, SQLException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", firstValidationError(bindingResult));
            return "register";
        }

        if (!isPasswordPolicyValid(user.getPassword())) {
            model.addAttribute("error", "La contraseña debe incluir letras, numeros y un simbolo especial");
            return "register";
        }

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

        String requestedUsername = user.getUsername() != null ? user.getUsername().trim() : null;
        String requestedEmail = user.getEmail() != null ? user.getEmail().trim() : null;

        if (requestedUsername == null || requestedUsername.length() < 4 || requestedUsername.length() > 15) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "El nombre de usuario debe tener entre 4 y 15 caracteres");
            return "edit_profile";
        }

        if (requestedEmail == null || requestedEmail.isBlank()) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "El correo electronico es obligatorio");
            return "edit_profile";
        }

        if (!user2.getUsername().equals(requestedUsername) && userService.usernameExists(requestedUsername)) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "Ese nombre de usuario ya existe");
            return "edit_profile";
        }

        if (!user2.getEmail().equals(requestedEmail) && userService.emailExists(requestedEmail)) {
            model.addAttribute("user", user2);
            model.addAttribute("error", "Ese correo electrónico ya está en uso");
            return "edit_profile";
        }

        user2.setEmail(requestedEmail);
        user2.setUsername(requestedUsername);

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

            if (newPassword.length() < 8 || newPassword.length() > 30 || !isPasswordPolicyValid(newPassword)) {
                model.addAttribute("user", user2);
                model.addAttribute("error",
                        "La nueva contraseña debe tener entre 8 y 30 caracteres, letras, numeros y un simbolo especial");
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

    private boolean isPasswordPolicyValid(String password) {
        return password != null && PASSWORD_POLICY.matcher(password).matches();
    }

    private String firstValidationError(BindingResult bindingResult) {
        if (!bindingResult.hasErrors() || bindingResult.getAllErrors().isEmpty()) {
            return "Hay datos invalidos en el formulario.";
        }
        return bindingResult.getAllErrors().get(0).getDefaultMessage();
    }
}
