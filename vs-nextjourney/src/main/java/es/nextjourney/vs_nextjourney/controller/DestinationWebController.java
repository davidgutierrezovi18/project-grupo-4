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

    // PROCESAR EL GUARDADO DEL DESTINO
    @PostMapping("/add_destination")
    public String newDestinationProcess(Model model, Destination destination, MultipartFile imageFile, Long id) throws IOException {
        
        // 1. Forzamos el ID si viene como parámetro separado (evita duplicados)
        if (id != null && id != 0) {
            destination.setId(id);
        }

        // 2. Lógica de Edición vs Nuevo
        if (destination.getId() != null && destination.getId() != 0) {
            Optional<Destination> oldDestOpt = destinationService.findById(destination.getId());
            
            if (oldDestOpt.isPresent()) {
                Destination oldDest = oldDestOpt.get();

                // A. Si no hay imagen nueva, recuperamos la que ya tenía
                if (imageFile == null || imageFile.isEmpty()) {
                    destination.setCoverImage(oldDest.getCoverImage());
                }

                // B. ¡FUNDAMENTAL! Recuperamos las listas para que no se borren
                // Al hacer esto, JPA entiende que quieres mantener los hijos existentes
                destination.setPlaces(oldDest.getPlaces());
                destination.setReviews(oldDest.getReviews());
            }
        } 
        // 3. Si es nuevo y no hay imagen, lanzamos el error
        else if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("imageError", true);
            model.addAttribute("destination", destination);
            model.addAttribute("isEditing", false);
            return "add_destination"; 
        }

        try {
            // 4. Procesar imagen nueva si el usuario la seleccionó
            if (imageFile != null && !imageFile.isEmpty()) {
                Image image = imageService.createImage(imageFile); 
                destination.setCoverImage(image);
            }
            
            // 5. Guardar: Al tener el ID y las listas recuperadas, hace un UPDATE limpio
            destinationService.save(destination);
            return "redirect:/destinations";
            
        } catch (Exception e) {
            model.addAttribute("imageError", true);
            model.addAttribute("destination", destination);
            return "add_destination";
        }
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
