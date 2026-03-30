package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.service.TravelService;
import jakarta.validation.Valid;

@Controller
public class TravelWebController {
    @Autowired
    private TravelService travelService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

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
            Image cover = imageService.createImage(coverImage);
            travel.setCoverImage(cover);
        }

        // Carrousel images
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : carouselImages) {
            if (!file.isEmpty()) {
                Image img = imageService.createImage(file);
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
    public String editTravel(@PathVariable Long id, Model model) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt == null) {
            return "redirect:/mytravels";
        }
        Travel travel = travelOpt.get();
        model.addAttribute("travel", travel);

        // Countries
        String countriesList = travel.getCountries();
        model.addAttribute("countries", (countriesList != null && !countriesList.isEmpty())
                ? String.join(", ", countriesList)
                : "");

        // Cities
        String citiesList = travel.getCities();
        model.addAttribute("cities",
                (citiesList != null && !citiesList.isEmpty()) ? String.join(", ", citiesList) : "");

        // Places
        String placesList = travel.getPlaces();
        model.addAttribute("places",
                (placesList != null && !placesList.isEmpty()) ? String.join(", ", placesList) : "");

        // Rating
        Integer ratingObj = travel.getRating();
        int rating = (ratingObj != null) ? ratingObj : 0;
        model.addAttribute("rating1", rating == 1);
        model.addAttribute("rating2", rating == 2);
        model.addAttribute("rating3", rating == 3);
        model.addAttribute("rating4", rating == 4);
        model.addAttribute("rating5", rating == 5);

        // Comments
        model.addAttribute("comment", travel.getComment() != null ? travel.getComment() : "");

        // Members
        String membersList = travel.getEmailsColaborators();
        model.addAttribute("members",
                (membersList != null && !membersList.isEmpty()) ? String.join(", ", membersList) : "");

        return "edit_travel";
    }

    // Edit travel - POST
    @PostMapping("/travel/{id}/edit")
    public String editTravelSubmit(@PathVariable Long id, @ModelAttribute Travel travel,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImage,
            @RequestParam(value = "carouselImageFiles", required = false) MultipartFile[] carouselImages,
            @RequestParam(value = "itineraryFile", required = false) MultipartFile itinerary,
            Principal principal) throws IOException {
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

        // Update cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            Image cover = imageService.createImage(coverImage);
            travel.setCoverImage(cover);
        } else {
            // Keep existing cover image
            travel.setCoverImage(existingTravel.getCoverImage());
        }

        // Update carousel images if provided
        if (carouselImages != null && carouselImages.length > 0) {
            List<Image> images = new ArrayList<>();
            for (MultipartFile file : carouselImages) {
                if (!file.isEmpty()) {
                    Image img = imageService.createImage(file);
                    img.setTravelImage(travel);
                    images.add(img);
                }
            }
            travel.setCarouselImagesUrls(images);
        } else {
            // Keep existing carousel images
            travel.setCarouselImagesUrls(existingTravel.getCarouselImages());
        }

        // Update itinerary if provided
        if (itinerary != null && !itinerary.isEmpty()) {
            travel.setItineraryUrl(itinerary.getOriginalFilename());
        } else {
            // Keep existing itinerary
            travel.setItineraryUrl(existingTravel.getItineraryUrl());
        }

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

        // Countries, cities and places lists
        model.addAttribute("countriesList",
                travel.getCountries() != null ? List.of(travel.getCountries().split(",")) : List.of());
        model.addAttribute("citiesList",
                travel.getCities() != null ? List.of(travel.getCities().split(",")) : List.of());
        model.addAttribute("placesList",
                travel.getPlaces() != null ? List.of(travel.getPlaces().split(",")) : List.of());

        // First carousel image
        List<Image> carouselImages = travel.getCarouselImages();
        for (int i = 0; i < carouselImages.size(); i++) {
            carouselImages.get(i).setActive(i == 0);
        }
        model.addAttribute("carouselImages", carouselImages);

        // Star ratig
        List<Integer> filledStars = new ArrayList<>();
        List<Integer> emptyStars = new ArrayList<>();
        // FIlled stars
        for (int i = 0; i < travel.getRating(); i++) {
            filledStars.add(i);
        }
        // Empty stars
        for (int i = travel.getRating(); i < 5; i++) {
            emptyStars.add(i);
        }
        model.addAttribute("filledStars", filledStars);
        model.addAttribute("emptyStars", emptyStars);

        // Get colaborator users by email
        List<User> collaborators = new ArrayList<>();
        String emails = travel.getEmailsColaborators();
        if (emails != null && !emails.trim().isEmpty()) {
            String[] emailArray = emails.split(",");
            for (String email : emailArray) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty()) {
                    Optional<User> userOpt = userRepository.findByEmail(trimmedEmail);
                    if (userOpt.isPresent()) {
                        collaborators.add(userOpt.get());
                    }
                }
            }
        }
        model.addAttribute("collaborators", collaborators);

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
