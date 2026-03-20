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
            return showErrorDestination(model, destination, false);
        }

        try {
            Image image = imageService.createImage(imageFile);
            destination.setCoverImage(image);
            destinationService.save(destination);
            return "redirect:/destinations";
        } catch (Exception e) {
            return showErrorDestination(model, destination, false);
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
    private String showErrorDestination(Model model, Destination destination, boolean isEditing) {
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


    // MOSTRAR FORMULARIO PARA AÑADIR NUEVO LUGAR
    @GetMapping("/destinations/{id}/add_place")
    public String showAddPlaceForm(Model model, @PathVariable long id) {
        Optional<Destination> destination = destinationService.findById(id);
        if (destination.isPresent()) {
            model.addAttribute("destination", destination.get());
            model.addAttribute("place", new Place()); // Objeto vacío para el formulario
            model.addAttribute("isEditing", false);
            return "add_place";
        }
        return "redirect:/destinations";
    }

    // PROCESAR EL GUARDADO DE UN NUEVO LUGAR
    @PostMapping("/destinations/{id}/add_place")
    public String savePlace(Model model, @PathVariable long id, Place place) {
        Optional<Destination> destinationOpt = destinationService.findById(id);
        
        if (destinationOpt.isPresent()) {
            // Forzamos que sea un registro nuevo para evitar errores de persistencia
            place.setId(null); 
            place.setDestination(destinationOpt.get());
            
            // Validación básica de nombre
            if (place.getName() == null || place.getName().isBlank()) {
                return showErrorPlace(model, destinationOpt.get(), place, false);
            }

            placeService.save(place); 
            return "redirect:/destinations/" + id;
        }
        return "redirect:/destinations";
    }

    // MOSTRAR FORMULARIO DE EDICIÓN DE LUGAR
    @GetMapping("/destinations/{destId}/places/{placeId}/edit")
    public String editPlaceForm(Model model, @PathVariable long destId, @PathVariable long placeId) {
        Optional<Destination> destination = destinationService.findById(destId);
        Optional<Place> place = placeService.findById(placeId);

        if (destination.isPresent() && place.isPresent()) {
            model.addAttribute("destination", destination.get());
            model.addAttribute("place", place.get()); // Pasamos el lugar existente
            model.addAttribute("isEditing", true);
            return "add_place";
        }
        return "redirect:/destinations/" + destId;
    }

    // PROCESAR LA EDICIÓN DEL LUGAR
    @PostMapping("/destinations/{destId}/places/{placeId}/edit")
    public String editPlaceProcess(Model model, @PathVariable long destId, @PathVariable long placeId, Place place) {
        Optional<Destination> destination = destinationService.findById(destId);
        Optional<Place> oldPlaceOpt = placeService.findById(placeId);

        if (destination.isPresent() && oldPlaceOpt.isPresent()) {
            try {
                // Mantenemos la identidad (ID) y vinculamos al destino padre
                place.setId(placeId); 
                place.setDestination(destination.get()); 
                
                placeService.save(place);
                return "redirect:/destinations/" + destId;
                
            } catch (Exception e) {
                return showErrorPlace(model, destination.get(), place, true);
            }
        }
        return "redirect:/destinations";
    }

    // BORRAR LUGAR
    @PostMapping("/destinations/{destId}/places/{placeId}/delete")
    public String deletePlace(@PathVariable long destId, @PathVariable long placeId) {
        placeService.delete(placeId);
        return "redirect:/destinations/" + destId; 
    }
    // Método auxiliar para lugares
    private String showErrorPlace(Model model, Destination destination, Place place, boolean isEditing) {
    model.addAttribute("destination", destination);
    model.addAttribute("place", place);
    model.addAttribute("isEditing", isEditing);
    return "add_place";
    }
}
