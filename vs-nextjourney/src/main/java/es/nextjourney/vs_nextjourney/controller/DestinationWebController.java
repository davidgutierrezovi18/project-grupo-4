package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.service.DestinationService;
import es.nextjourney.vs_nextjourney.service.ImageService;

@Controller
public class DestinationWebController {

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private ImageService imageService;

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            model.addAttribute("logged", true);
            model.addAttribute("userName", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));
        } else {
            model.addAttribute("logged", false);
        }
    }

    // LISTAR TODOS LOS DESTINOS
    @GetMapping("/destinations")
    public String showDestinations(Model model) {
        model.addAttribute("destinations", destinationService.findAll());
        return "destinations";
    }

    // VER UN DESTINO DETALLADO (Y SUS LUGARES)
    @GetMapping("/destinations/{id}")
    public String showDestination(Model model, @PathVariable long id) {
        Optional<Destination> destination = destinationService.findById(id);
        if (destination.isPresent()) {
            model.addAttribute("destination", destination.get());
            model.addAttribute("places", destination.get().getPlaces());
            return "one_destination";
        } else {
            return "redirect:/destinations";
        }
    }

    // MOSTRAR FORMULARIO DE NUEVO DESTINO
    @GetMapping("/add_destination")
    public String newDestination(Model model) {
        return "add_destination";
    }

    // PROCESAR EL GUARDADO DEL DESTINO
    @PostMapping("/add_destination")
    public String newDestinationProcess(Destination destination, MultipartFile imageField) throws IOException {
        
        if (imageField != null && !imageField.isEmpty()) {
            Image image = imageService.createImage(imageField.getInputStream());
            destination.setCoverImage(image);
        }
        
        destinationService.save(destination);
        return "redirect:/destinations/" + destination.getId();
    }

    // BORRAR DESTINO (Opcional, siguiendo el ejemplo del profe)
    @PostMapping("/destinations/{id}/delete")
    public String deleteDestination(@PathVariable long id) {
        destinationService.delete(id);
        return "redirect:/destinations";
    }
}
