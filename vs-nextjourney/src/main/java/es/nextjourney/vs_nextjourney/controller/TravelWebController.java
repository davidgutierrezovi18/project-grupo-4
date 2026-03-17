package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.TravelService;

@Controller
public class TravelWebController {
    @Autowired
    private TravelService travelService;

    // All the travels of a specific user
    @GetMapping("/mytravels")
    public String myTravels(Model model, Principal principal) {
        String username = principal.getName();
        List<Travel> travels = travelService.findByOwnerName(username);
        model.addAttribute("travels", travels);
        return "mytravels";
    }


    // Create travel - GET
    @GetMapping("/travel/new")
    public String newTravelGet(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }
        model.addAttribute("travel", new Travel());
        return "create_new_travel";
    }

    /*
    // Create travel - POST
    @PostMapping("/travel/new")
    public String newTravelPost(@ModelAttribute Travel travel, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }
        travel.setOwnerName(principal.getName());
        travelService.save(travel);
        return "redirect:/mytravels";
    }
        */

    // Create travel - POST
    @PostMapping("/travel/new")
    public String newTravelPost(@ModelAttribute Travel travel,
            @RequestParam("coverImageFile") MultipartFile coverImage,
            @RequestParam("carouselImageFiles") MultipartFile[] carouselImages,
            @RequestParam("itineraryFile") MultipartFile itinerary,
            Principal principal) throws IOException {
        if (principal == null) {
            return "redirect:/sign_in";
        }
        travel.setOwnerName(principal.getName());

        // Cover image
        if (!coverImage.isEmpty()) {
            Image cover = new Image();
            travel.setCoverImage(cover);
        }

        // Carrousel images
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : carouselImages) {
            if (!file.isEmpty()) {
                Image img = new Image();
                img.setTravelImage(travel);
                images.add(img);
            }
        }
        travel.setCarouselImagesUrls(images);

        // Itinerary PDF
        if (!itinerary.isEmpty()) {
            travel.setItineraryUrl(itinerary.getOriginalFilename());
        }
        travelService.save(travel);
        return "redirect:/mytravels";
    }

    // Edit travel - GET
    @GetMapping("/travel/{id}/edit")
    public String editTravelForm(@PathVariable Long id, Model model, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error404";
        }
        Travel travel = travelOpt.get();
        if (!travel.getOwnerName().equals(principal.getName())) {
            return "error403";
        }
        model.addAttribute("travel", travel);
        return "edit_travel";
    }

    // Edit travel - POST
    @PostMapping("/travel/{id}/edit")
    public String editTravelSubmit(@PathVariable Long id, @ModelAttribute Travel travel, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error404";
        }
        Travel existingTravel = travelOpt.get();
        if (!existingTravel.getOwnerName().equals(principal.getName())) {
            return "error403";
        }
        travel.setId(existingTravel.getId());
        travel.setOwnerName(existingTravel.getOwnerName());
        travelService.save(travel);
        return "redirect:/travel/" + id;
    }

    // One travel
    @GetMapping("/travel/{id}")
    public String oneTravel(@PathVariable Long id, Model model, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error404";
        }
        Travel travel = travelOpt.get();
        model.addAttribute("travel", travel);
        // Process elements split by comas
        model.addAttribute("countriesList",
                travel.getCountries() != null ? List.of(travel.getCountries().split(",")) : List.of());
        model.addAttribute("citiesList",
                travel.getCities() != null ? List.of(travel.getCities().split(",")) : List.of());
        model.addAttribute("placesList",
                travel.getPlaces() != null ? List.of(travel.getPlaces().split(",")) : List.of());
        return "one_travel";
    }

    // Delete travel
    @PostMapping("/travel/{id}/delete")
    public String deleteTravel(@PathVariable Long id, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error404";
        }
        Travel travel = travelOpt.get();
        if (!travel.getOwnerName().equals(principal.getName())) {
            return "error403";
        }
        travelService.deleteById(id);
        return "redirect:/mytravels";
    }

}
