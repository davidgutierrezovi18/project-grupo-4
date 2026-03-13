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
        return "add_destination";
    }

    // PROCESAR EL GUARDADO DEL DESTINO
    @PostMapping("/add_destination")
    public String newDestinationProcess(Destination destination, MultipartFile imageFile) throws IOException {
        
        if (imageFile != null && !imageFile.isEmpty()) {
           Image image = imageService.createImage(imageFile); 
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
