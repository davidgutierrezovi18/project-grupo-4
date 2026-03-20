package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.service.DestinationService;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.service.PlaceService;

@Controller
public class DestinationWebController {

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private ImageService imageService;

    @Autowired
        private PlaceService placeService;

    // LISTAR TODOS LOS DESTINOS
    @GetMapping("/destinations")
    public String showDestinations(Model model,HttpSession session) {
        model.addAttribute("destinations", destinationService.findAll());
        if(session != null){

            model.addAttribute("isLogged", true);

        }
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
        model.addAttribute("isEditing", false);
        return "add_destination";
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/destinations/{id}/edit")
    public String editDestination(Model model, @PathVariable long id) {
        Optional<Destination> destination = destinationService.findById(id);
        if (destination.isPresent()) {
            model.addAttribute("destination", destination.get());
            model.addAttribute("isEditing", true); // Esto sirve para cambiar el título en el HTML
            return "add_destination";
        }
        return "redirect:/destinations";
    }

    // --- CREACIÓN ---
    @PostMapping("/add_destination")
    public String newDestinationProcess(Model model, Destination destination, MultipartFile imageFile) throws IOException {
        // Si no hay imagen en un registro nuevo, error
        if (imageFile == null || imageFile.isEmpty()) {
            return showError(model, destination, false);
        }

        try {
            Image image = imageService.createImage(imageFile);
            destination.setCoverImage(image);
            destinationService.save(destination);
            return "redirect:/destinations";
        } catch (Exception e) {
            return showError(model, destination, false);
        }
    }

    // --- EDICIÓN ---
    @PostMapping("/destinations/{id}/edit")
    public String editDestinationProcess(Model model, Destination destination, MultipartFile imageFile, @PathVariable long id) throws IOException {
        Optional<Destination> oldDestOpt = destinationService.findById(id);
        
        if (oldDestOpt.isPresent()) {
            Destination oldDest = oldDestOpt.get();
            
            // Mantenemos la identidad y las listas
            destination.setId(id);
            destination.setPlaces(oldDest.getPlaces());
            destination.setReviews(oldDest.getReviews());

            // Lógica de imagen: si no hay nueva, mantenemos la vieja
            if (imageFile == null || imageFile.isEmpty()) {
                destination.setCoverImage(oldDest.getCoverImage());
            } else {
                Image image = imageService.createImage(imageFile);
                destination.setCoverImage(image);
            }

            destinationService.save(destination);
            return "redirect:/destinations";
        }
        
        return "redirect:/destinations";
    }

    // Método auxiliar para no repetir código de error
    private String showError(Model model, Destination destination, boolean isEditing) {
        model.addAttribute("imageError", true);
        model.addAttribute("destination", destination);
        model.addAttribute("isEditing", isEditing);
        return "add_destination";
    }

    // BORRAR DESTINO 
    @PostMapping("/destinations/{id}/delete")
    public String deleteDestination(@PathVariable long id) {
        destinationService.delete(id);
        return "redirect:/destinations";
    }

    // MOSTRAR FORMULARIO PARA AÑADIR LUGAR
    @GetMapping("/destinations/{id}/add_place")
    public String showAddPlaceForm(Model model, @PathVariable long id) {
        Optional<Destination> destination = destinationService.findById(id);
        if (destination.isPresent()) {
            model.addAttribute("destination", destination.get());
            return "add_place"; // Este es el nombre de tu archivo .html
        } else {
            return "redirect:/destinations";
        }
    }

    // PROCESAR EL GUARDADO DEL LUGAR
    @PostMapping("/destinations/{id}/add_place")
    public String savePlace(@PathVariable long id, Place place) {
        Optional<Destination> destination = destinationService.findById(id);
        if (destination.isPresent()) {
            // MUY IMPORTANTE: Asociamos el lugar con su destino
            place.setDestination(destination.get());
            placeService.save(place); 
            
            return "redirect:/destinations/" + id;
        }
        return "redirect:/destinations";
    }
}
