package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributesAdvice {

    @ModelAttribute
    public void addAuthAttributes(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        boolean isLogged = principal != null;
        boolean isAdmin = isLogged && request.isUserInRole("ADMIN");

        model.addAttribute("isLogged", isLogged);
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("logged", isLogged);
        model.addAttribute("admin", isAdmin);
        model.addAttribute("userName", isLogged ? principal.getName() : null);
    }
}