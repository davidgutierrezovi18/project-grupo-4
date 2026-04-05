package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    // All the destinations
    @GetMapping("/destinations")
    public String showDestinations(Model model,HttpSession session) {
        model.addAttribute("destinations", destinationService.findAll());
        if(session != null){

            model.addAttribute("isLogged", true);

        }
        return "destinations";
    }

    // One destination with its places
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

    // Create destination - GET
    @GetMapping("/add_destination")
    public String newDestination(Model model) {
        model.addAttribute("isEditing", false);
        return "add_destination";
    }

    // Create destination - POST
    @PostMapping("/add_destination")
    public String newDestinationProcess(Model model, @Valid Destination destination, BindingResult bindingResult,
            MultipartFile imageFile) throws IOException {

        if (bindingResult.hasErrors()) {
            return showErrorDestination(model, destination, false, firstValidationError(bindingResult), false);
        }

        if (imageFile == null || imageFile.isEmpty()) {
            return showErrorDestination(model, destination, false, "La imagen del destino es obligatoria.", true);
        }

        try {
            Image image = imageService.createImage(imageFile);
            destination.setCoverImage(image);
            destinationService.save(destination);
            return "redirect:/destinations";
        } catch (Exception e) {
            return showErrorDestination(model, destination, false,
                    "No se ha podido guardar el destino. Revisa los datos e intentalo de nuevo.", false);
        }
    }

    // Edit destination - GET
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

    // Edit destination - POST
    @PostMapping("/destinations/{id}/edit")
    public String editDestinationProcess(Model model, @Valid Destination destination, BindingResult bindingResult,
            MultipartFile imageFile, @PathVariable long id) throws IOException {
        Optional<Destination> oldDestOpt = destinationService.findById(id);
        
        if (oldDestOpt.isPresent()) {
            Destination oldDest = oldDestOpt.get();

            if (bindingResult.hasErrors()) {
                destination.setId(id);
                destination.setCoverImage(oldDest.getCoverImage());
                destination.setPlaces(oldDest.getPlaces());
                destination.setReviews(oldDest.getReviews());
                return showErrorDestination(model, destination, true, firstValidationError(bindingResult), false);
            }
            
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

    // Auxiliar method for not repeating error code
    private String showErrorDestination(Model model, Destination destination, boolean isEditing, String errorMessage,
            boolean imageError) {
        model.addAttribute("imageError", imageError);
        model.addAttribute("error", errorMessage);
        model.addAttribute("destination", destination);
        model.addAttribute("isEditing", isEditing);
        return "add_destination";
    }

    // Delete destination 
    @PostMapping("/destinations/{id}/delete")
    public String deleteDestination(@PathVariable long id) {
        destinationService.delete(id);
        return "redirect:/destinations";
    }

    // Add place to destination - GET
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

    // Add place to destination - POST
    @PostMapping("/destinations/{id}/add_place")
    public String savePlace(Model model, @PathVariable long id, @Valid Place place, BindingResult bindingResult) {
        Optional<Destination> destinationOpt = destinationService.findById(id);
        
        if (destinationOpt.isPresent()) {
            // Forzamos que sea un registro nuevo para evitar errores de persistencia
            place.setId(null); 
            place.setDestination(destinationOpt.get());

            if (bindingResult.hasErrors()) {
                return showErrorPlace(model, destinationOpt.get(), place, false, firstValidationError(bindingResult));
            }

            placeService.save(place); 
            return "redirect:/destinations/" + id;
        }
        return "redirect:/destinations";
    }

    // Edit a place of a destination - GET
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

    // Edit a place of a destination - POST
    @PostMapping("/destinations/{destId}/places/{placeId}/edit")
    public String editPlaceProcess(Model model, @PathVariable long destId, @PathVariable long placeId,
            @Valid Place place, BindingResult bindingResult) {
        Optional<Destination> destination = destinationService.findById(destId);
        Optional<Place> oldPlaceOpt = placeService.findById(placeId);

        if (destination.isPresent() && oldPlaceOpt.isPresent()) {
            // Mantenemos la identidad (ID) y vinculamos al destino padre
            place.setId(placeId);
            place.setDestination(destination.get());

            if (bindingResult.hasErrors()) {
                return showErrorPlace(model, destination.get(), place, true, firstValidationError(bindingResult));
            }

            placeService.save(place);
            return "redirect:/destinations/" + destId;
        }
        return "redirect:/destinations";
    }

    // Delete place of a destination
    @PostMapping("/destinations/{destId}/places/{placeId}/delete")
    public String deletePlace(@PathVariable long destId, @PathVariable long placeId) {
        placeService.delete(placeId);
        return "redirect:/destinations/" + destId; 
    }

    // Aux method for places
    private String showErrorPlace(Model model, Destination destination, Place place, boolean isEditing,
            String errorMessage) {
        model.addAttribute("destination", destination);
        model.addAttribute("place", place);
        model.addAttribute("isEditing", isEditing);
        model.addAttribute("error", errorMessage);
        return "add_place";
    }

    private String firstValidationError(BindingResult bindingResult) {
        if (!bindingResult.hasErrors() || bindingResult.getAllErrors().isEmpty()) {
            return "Hay datos invalidos en el formulario.";
        }
        return bindingResult.getAllErrors().get(0).getDefaultMessage();
    }
}
