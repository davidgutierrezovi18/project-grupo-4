package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import es.nextjourney.vs_nextjourney.service.FileStorageService;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.service.TravelService;

@Controller
public class TravelWebController {
    @Autowired
    private TravelService travelService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // All the travels of a specific user
    @GetMapping("/mytravels")
    public String myTravels(Model model, Principal principal) {
        String username = principal.getName();
        List<Travel> travels = travelService.findAllByUser(username);
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
    public String newTravelPost(@Valid @ModelAttribute("travel") Travel travel, BindingResult bindingResult,
            @RequestParam("coverImageFile") MultipartFile coverImage,
            @RequestParam("carouselImageFiles") MultipartFile[] carouselImages,
            @RequestParam("itineraryFile") MultipartFile itinerary,
            Principal principal,
            Model model) throws IOException {
        
        if (principal == null) {
            return "redirect:/sign_in";
        }
        
        // security validations
        try {
            validateImage(coverImage); // validates the cover is an image
            for (MultipartFile f : carouselImages) validateImage(f); // validates the carousel images

            // cleans the HTML of description and comment (XSS)
            travel.setDescription(Jsoup.clean(travel.getDescription(), Safelist.basic()));
            travel.setComment(Jsoup.clean(travel.getComment(), Safelist.basic()));
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "create_new_travel";
        }
        
        User owner = userRepository.findByUsername(principal.getName()).orElseThrow();
        travel.setOwnerName(owner.getUsername());

        // Make sure userTravels and owner travels lists are initialized
        if (travel.getUserTravels() == null) {
            travel.setUserTravels(new ArrayList<>());
        }
        if (owner.getTravels() == null) {
            owner.setTravels(new ArrayList<>());
        }

        // Add travel owner
        travel.getUserTravels().add(owner);

        

        // Validate form
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", firstValidationError(bindingResult));
            return "create_new_travel";
        }

        if (travel.getStartDate() != null && travel.getEndDate() != null
                && travel.getEndDate().isBefore(travel.getStartDate())) {
            model.addAttribute("error", "La fecha de fin no puede ser anterior a la fecha de inicio");
            return "create_new_travel";
        }

        if (coverImage == null || coverImage.isEmpty()) {
            model.addAttribute("error", "La imagen de portada es obligatoria");
            return "create_new_travel";
        }

        // Cover image
        if (!coverImage.isEmpty()) {
            Image cover = imageService.createImage(coverImage);
            travel.setCoverImage(cover);
        }


        travelService.save(travel);

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

        // Itinerary PDF - save in disk
        if (!itinerary.isEmpty()) {
            try {
                // Save file to disk and store path in DB
                String filePath = fileStorageService.storeFile(itinerary);
                travel.setItineraryPath(filePath);
                // Mantein the original filename for display purposes
                travel.setItineraryUrl(itinerary.getOriginalFilename());
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
                return "create_new_travel";
            }
        }

        // Collaborators
        addCollaborators(travel);


        // Final save
        travelService.save(travel);

        return "redirect:/mytravels";
    }

    // Edit travel - GET
    @GetMapping("/travel/{id}/edit")
    public String editTravel(@PathVariable Long id, Model model, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "redirect:/mytravels";
        }
        Travel travel = travelOpt.get();

        if (!travel.getOwnerName().equals(principal.getName())) {
            return "error/403"; // property verification
        }

        populateEditTravelModel(model, travel);

        return "edit_travel";
    }

    // Edit travel - POST
    @PostMapping("/travel/{id}/edit")
    public String editTravelSubmit(@PathVariable Long id, @Valid @ModelAttribute("travel") Travel travel,
            BindingResult bindingResult,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImage,
            @RequestParam(value = "carouselImageFiles", required = false) MultipartFile[] carouselImages,
            @RequestParam(value = "itineraryFile", required = false) MultipartFile itinerary,
            Principal principal,
            Model model) throws IOException {
        
        if (principal == null) {
            return "redirect:/sign_in";
        }

        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error/404";
        }
        Travel existingTravel = travelOpt.get();
        if (!existingTravel.getOwnerName().equals(principal.getName())) {
            return "error/403";
        }

        // security validations
        try {
            validateImage(coverImage);
            for (MultipartFile f : carouselImages) validateImage(f);
            // XSS protection
            if (travel.getDescription() != null) {
                travel.setDescription(Jsoup.clean(travel.getDescription(), Safelist.basic()));
            }
            if (travel.getComment() != null) {
                travel.setComment(Jsoup.clean(travel.getComment(), Safelist.basic()));
            }

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            populateEditTravelModel(model, existingTravel);
            return "edit_travel";
        }

        travel.setId(existingTravel.getId());
        travel.setOwnerName(existingTravel.getOwnerName());

        // Validate form
        if (bindingResult.hasErrors()) {
            travel.setCoverImage(existingTravel.getCoverImage());
            travel.setCarouselImagesUrls(existingTravel.getCarouselImages());
            travel.setItineraryUrl(existingTravel.getItineraryUrl());
            model.addAttribute("error", firstValidationError(bindingResult));
            populateEditTravelModel(model, travel);
            return "edit_travel";
        }

        if (travel.getStartDate() != null && travel.getEndDate() != null
                && travel.getEndDate().isBefore(travel.getStartDate())) {
            travel.setCoverImage(existingTravel.getCoverImage());
            travel.setCarouselImagesUrls(existingTravel.getCarouselImages());
            travel.setItineraryUrl(existingTravel.getItineraryUrl());
            model.addAttribute("error", "La fecha de fin no puede ser anterior a la fecha de inicio");
            populateEditTravelModel(model, travel);
            return "edit_travel";
        }

        // Update cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            Image cover = imageService.createImage(coverImage);
            imageService.save(cover);
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
                    imageService.save(img);
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
            // Delete old file if exists
            fileStorageService.deleteFile(existingTravel.getItineraryPath());
            // Save new
            String filePath = fileStorageService.storeFile(itinerary);
            travel.setItineraryPath(filePath);
            travel.setItineraryUrl(itinerary.getOriginalFilename());
        } else {
            // Keep existing
            travel.setItineraryPath(existingTravel.getItineraryPath());
            travel.setItineraryUrl(existingTravel.getItineraryUrl());
        }

        // Collaborators
        syncUsers(travel, principal);

        travelService.save(travel);
        return "redirect:/travel/" + id;
    }

    // One travel
    @GetMapping("/travel/{id}")
    public String oneTravel(@PathVariable Long id, Model model, Principal principal) {
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return "error/404";
        }

        Travel travel = travelOpt.get();
        String username = principal != null ? principal.getName() : null;

        boolean hasAccess = isAuthorizedForTravel(travel, username);
        boolean isOwner = principal != null && travel.getOwnerName().equals(principal.getName());

        if (!hasAccess) {
            return "error/403"; // acceso prohibido
        }
        model.addAttribute("travel", travel);

        model.addAttribute("isOwner", isOwner);

        // Countries, cities and places lists
        List<String> countriesList = travel.getCountries() != null && !travel.getCountries().isEmpty()
                ? List.of(travel.getCountries().split(","))
                : List.of();
        List<String> citiesList = travel.getCities() != null && !travel.getCities().isEmpty()
                ? List.of(travel.getCities().split(","))
                : List.of();
        List<String> placesList = travel.getPlaces() != null && !travel.getPlaces().isEmpty()
                ? List.of(travel.getPlaces().split(","))
                : List.of();

        model.addAttribute("countriesList", countriesList);
        model.addAttribute("citiesList", citiesList);
        model.addAttribute("placesList", placesList);

        // Booleans to check if there are countries, cities or places
        model.addAttribute("hasCountries", !countriesList.isEmpty());
        model.addAttribute("hasCities", !citiesList.isEmpty());
        model.addAttribute("hasPlaces", !placesList.isEmpty());

        // First carousel image
        List<Image> carouselImages = travel.getCarouselImages();
        for (int i = 0; i < carouselImages.size(); i++) {
            carouselImages.get(i).setActive(i == 0);
        }
        model.addAttribute("carouselImages", carouselImages);
        // Boolean to check if there are carousel images
        model.addAttribute("hasCarouselImages", !carouselImages.isEmpty());

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
            return "error/404";
        }
        Travel travel = travelOpt.get();
        if (!travel.getOwnerName().equals(principal.getName())) {
            return "error/403";
        }
        travelService.deleteById(id);
        return "redirect:/mytravels";
    }

    // Download itinerary
    @GetMapping("/travel/{id}/itinerary")
    public void downloadItinerary(@PathVariable Long id, Principal principal, HttpServletResponse response)
            throws IOException {

        // 1. verify if user is logged in
        if (principal == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 2. verify that the travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Travel travel = travelOpt.get();

        // 3. verify access: only owner and collaborators can download the itinerary
        boolean hasAccess = principal != null && (travel.getOwnerName().equals(principal.getName()) ||
                travel.getUserTravels().stream().anyMatch(u -> u.getUsername().equals(principal.getName())));

        if (!hasAccess) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 4. verify if the travel has itinerary 
        String filePath = travel.getItineraryPath();
        if (filePath == null || filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path path = fileStorageService.getFilePath(filePath);
        if (!Files.exists(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 5. configure headers for download
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                travel.getItineraryUrl() + "\"");
        response.setContentLengthLong(Files.size(path));

        // 6. write file to response
        try (InputStream inputStream = Files.newInputStream(path);
                OutputStream outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
        }
    }

    // AUXILIARY METHODS
    private void populateEditTravelModel(Model model, Travel travel) {
        model.addAttribute("travel", travel);

        int rating = travel.getRating();
        model.addAttribute("rating1", rating == 1);
        model.addAttribute("rating2", rating == 2);
        model.addAttribute("rating3", rating == 3);
        model.addAttribute("rating4", rating == 4);
        model.addAttribute("rating5", rating == 5);

        model.addAttribute("members",
                travel.getEmailsColaborators() != null ? travel.getEmailsColaborators() : "");
    }

    private String firstValidationError(BindingResult bindingResult) {
        if (!bindingResult.hasErrors() || bindingResult.getAllErrors().isEmpty()) {
            return "Hay datos invalidos en el formulario.";
        }
        return bindingResult.getAllErrors().get(0).getDefaultMessage();
    }

    private void addCollaborators(Travel travel) {
        String emails = travel.getEmailsColaborators();

        if (emails == null || emails.trim().isEmpty())
            return;

        for (String email : emails.split(",")) {
            userRepository.findByEmail(email.trim()).ifPresent(user -> {
                if (!travel.getUserTravels().contains(user)) {
                    travel.getUserTravels().add(user);
                }
            });
        }
    }

    private void syncUsers(Travel travel, Principal principal) {

        List<User> users = new ArrayList<>();

        // Owner
        User owner = userRepository.findByUsername(principal.getName()).orElseThrow();
        users.add(owner);

        // Collaborators
        String emails = travel.getEmailsColaborators();

        if (emails != null && !emails.trim().isEmpty()) {
            for (String email : emails.split(",")) {
                userRepository.findByEmail(email.trim()).ifPresent(users::add);
            }
        }

        travel.setUserTravels(users);
    }

    private boolean isAuthorizedForTravel(Travel travel, String username) {
        // the owner can access the travel
        if (travel.getOwnerName().equals(username)) {
            return true;
        }
        // collaborators can access the travel
        return travel.getUserTravels() != null && travel.getUserTravels().stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return;
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo " + file.getOriginalFilename() + " no es una imagen válida.");
        }
        String name = file.getOriginalFilename().toLowerCase();
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
            throw new RuntimeException("Solo se admiten imágenes JPG o PNG.");
        }
    }

    private void sanitizeTravelData(Travel travel) {
        if (travel.getDescription() != null) {
            travel.setDescription(Jsoup.clean(travel.getDescription(), Safelist.basic()));
        }
        if (travel.getComment() != null) {
            travel.setComment(Jsoup.clean(travel.getComment(), Safelist.basic()));
        }
    }

    

}