package es.nextjourney.vs_nextjourney.controller;

import es.nextjourney.vs_nextjourney.dto.TravelMapper;
import es.nextjourney.vs_nextjourney.dto.ImageDTO;
import es.nextjourney.vs_nextjourney.dto.ImageMapper;
import es.nextjourney.vs_nextjourney.dto.TravelDTO;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.FileStorageService;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.service.TravelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/v1/travels")
public class TravelRestController {

    @Autowired
    private TravelService travelService;

    @Autowired
    private TravelMapper travelMapper;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // GET all travels - admin sees all, regular users see only their own
    @GetMapping({"", "/"})
    public ResponseEntity<Page<TravelDTO>> getMyTravels(Principal principal, Pageable pageable, Authentication authentication) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<TravelDTO> travels;

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        if (isAdmin) {
            // admin users see all travels in the database
            travels = travelService.findAllPaginated(pageable)
                    .map(travelMapper::toDTO);
        } else {
            // logged users see only their own travels (owned or as collaborator)
            travels = travelService.findAllByUser(principal.getName(), pageable)
                    .map(travelMapper::toDTO);
        }
        
        return ResponseEntity.ok(travels);
    }

    // GET one travel - admin can see any travel, regular users see only their own
    @GetMapping("/{id}")
    public ResponseEntity<TravelDTO> getOneTravel(@PathVariable Long id, Principal principal, Authentication authentication) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user is admin - admins can see any travel
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        if (!isAdmin && !isAuthorizedForTravel(travel, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(travelMapper.toDTO(travel));
    }

    // POST create travel
    @PostMapping
    public ResponseEntity<TravelDTO> createTravel(@RequestBody TravelDTO travelDTO, Principal principal, Authentication authentication) {
        
        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        Travel travel = travelMapper.toDomain(travelDTO);

        if (!(isAdmin && travel.getOwnerName() != null)) {
            travel.setOwnerName(principal.getName());
        }

        if (travel.getCoverImage() == null) {
            Image coverImage = new Image();
            coverImage.setContentType("application/octet-stream");
            travel.setCoverImage(coverImage);
        }

        travelService.save(travel);
        TravelDTO createdDto = travelMapper.toDTO(travel);
        return ResponseEntity.created(URI.create("/api/v1/travels/" + travel.getId()))
                .body(createdDto);
    }

    // PUT update travel
    @PutMapping("/{id}")
    public ResponseEntity<TravelDTO> updateTravel(@PathVariable Long id, @RequestBody TravelDTO travelDTO, Principal principal, Authentication authentication) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the travel is empty or exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel existingTravel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if the user is the owner or admin (collaborator can not update)
        if (!isAdmin && !existingTravel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // new informartion to update the travel
        // TODO: REVISAR
        Travel updatedTravel = travelMapper.toDomain(travelDTO);
        updatedTravel.setId(existingTravel.getId());
        updatedTravel.setOwnerName(existingTravel.getOwnerName());
        updatedTravel.setCoverImage(existingTravel.getCoverImage());
        updatedTravel.setCarouselImagesUrls(existingTravel.getCarouselImages());
        updatedTravel.setItineraryUrl(existingTravel.getItineraryUrl());
        updatedTravel.setItineraryPath(existingTravel.getItineraryPath());
        updatedTravel.setEmailsColaborators(existingTravel.getEmailsColaborators());
        updatedTravel.setUserTravels(existingTravel.getUserTravels());

        travelService.save(updatedTravel);
        return ResponseEntity.ok(travelMapper.toDTO(updatedTravel));
    }

    // DELETE travel
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTravel(@PathVariable Long id, Principal principal, Authentication authentication) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the travel is empty or exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if the user is the owner or admin (collaborator can not delete)
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        travelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // POST add image to carouselImages in travel
    @PostMapping("/{id}/images")
    public ResponseEntity<ImageDTO> createTravelImage(@PathVariable long id, 
            @RequestParam("imageFile") MultipartFile imageFile, 
            Principal principal, Authentication authentication) throws IOException {

        // check if user is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();
        
        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if user is owner or admin (only owner/admin can add images)
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Image image = imageService.createImage(imageFile);
        image.setTravelImage(travel);
        imageService.save(image);
        image.setTravelImage(travel);
        travel.getCarouselImages().add(image);
        travelService.save(travel);

        return ResponseEntity.status(HttpStatus.CREATED).body(imageMapper.toDTO(image));
    }

    // POST add cover image to travel
    @PostMapping("/{id}/coverImage")
    public ResponseEntity<ImageDTO> createTravelCoverImage(@PathVariable long id,
            @RequestParam("coverImageFile") MultipartFile imageFile,
            Principal principal, Authentication authentication) throws IOException {

        // check if user is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if user is owner or admin (only owner/admin can add images)
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // check if file is empty
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Image existingCover = travel.getCoverImage();
        if (existingCover != null && existingCover.getId() != null) {
            imageService.deleteImageById(existingCover.getId());
        }

        Image coverImage = imageService.createImage(imageFile);
        coverImage.setTravelImage(travel);
        travel.setCoverImage(coverImage);
        travelService.save(travel);

        return ResponseEntity.status(HttpStatus.CREATED).body(imageMapper.toDTO(coverImage));
    }

    // DELETE remove cover image from travel
    @DeleteMapping("/{id}/coverImage")
    public ResponseEntity<Void> deleteTravelCoverImage(@PathVariable long id,
            Principal principal, Authentication authentication) {

        // check if user is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if user is owner or admin (only owner/admin can delete images)
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Image existingCover = travel.getCoverImage();
        if (existingCover == null || existingCover.getId() == null) {
            return ResponseEntity.notFound().build();
        }

        Long oldCoverId = existingCover.getId();
        Image placeholder = new Image();
        placeholder.setContentType("application/octet-stream");
        travel.setCoverImage(placeholder);
        travelService.save(travel);
        imageService.deleteImageById(oldCoverId);

        return ResponseEntity.noContent().build();
    }

    // POST upload itinerary PDF for travel
    @PostMapping("/{id}/itinerary")
    public ResponseEntity<TravelDTO> uploadTravelItinerary(@PathVariable long id,
            @RequestParam("itineraryFile") MultipartFile itineraryFile,
            Principal principal, Authentication authentication) throws IOException {

        // verify the user is authenticated
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // load the travel and verify it exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // only the travel owner or admin can upload the itinerary PDF
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // reject empty file uploads
        if (itineraryFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // delete existing itinerary file from disk if present
        String existingPath = travel.getItineraryPath();
        if (existingPath != null && !existingPath.isBlank()) {
            fileStorageService.deleteFile(fileStorageService.getFilePath(existingPath).toString());
        }

        // store the new PDF on disk and save the new file name in travel
        String storedFilename = fileStorageService.storeFile(itineraryFile);
        travel.setItineraryPath(storedFilename);
        travel.setItineraryUrl(itineraryFile.getOriginalFilename());
        travelService.save(travel);

        return ResponseEntity.created(URI.create("/api/v1/travels/" + id + "/itinerary"))
                .body(travelMapper.toDTO(travel));
    }

    // DELETE remove itinerary PDF from travel
    @DeleteMapping("/{id}/itinerary")
    public ResponseEntity<Void> deleteTravelItinerary(@PathVariable long id,
            Principal principal, Authentication authentication) {

        // verify the user is authenticated
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // load the travel and verify it exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();

        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // only the travel owner or admin can remove the itinerary PDF
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // if there is no stored itinerary, return 404
        String existingPath = travel.getItineraryPath();
        if (existingPath == null || existingPath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        // delete the stored file and clear travel PDF metadata
        fileStorageService.deleteFile(fileStorageService.getFilePath(existingPath).toString());
        travel.setItineraryPath(null);
        travel.setItineraryUrl(null);
        travelService.save(travel);

        return ResponseEntity.noContent().build();
    }

    // DELETE remove image from carouselImages in travel
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteTravelImage(@PathVariable long id, 
            @PathVariable long imageId,
            Principal principal, Authentication authentication) throws IOException {

        // check if user is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if travel exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Travel travel = travelOpt.get();
        
        // check if the user has ADMIN role
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        // check if user is owner or admin (only owner/admin can delete images)
        if (!isAdmin && !travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // check if image exists and belongs to this travel
        Optional<Image> imageOpt = travel.getCarouselImages().stream()
                .filter(img -> img.getId() != null && img.getId().equals(imageId))
                .findFirst();

        if (imageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Image image = imageOpt.get();
        travel.getCarouselImages().remove(image);
        imageService.deleteImageById(imageId);
        travelService.save(travel);

        return ResponseEntity.noContent().build();
    }

    // AUXILIARY METHODS
    private boolean isAuthorizedForTravel(Travel travel, String username) {
        // the owner can access the travel
        if (travel.getOwnerName().equals(username)) {
            return true;
        }
        // collaborators can access the travel
        return travel.getUserTravels() != null && travel.getUserTravels().stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }
}
