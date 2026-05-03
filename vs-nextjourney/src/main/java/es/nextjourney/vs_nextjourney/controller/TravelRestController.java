package es.nextjourney.vs_nextjourney.controller;

import es.nextjourney.vs_nextjourney.dto.TravelDTO;
import es.nextjourney.vs_nextjourney.dto.TravelMapper;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.TravelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/travels")
public class TravelRestController {

    @Autowired
    private TravelService travelService;

    @Autowired
    private TravelMapper travelMapper;

    // GET my travels
    @GetMapping("/")
    public ResponseEntity<List<TravelDTO>> getMyTravels(Principal principal) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Travel> travels = travelService.findAllByUser(principal.getName());
        return ResponseEntity.ok(travelMapper.toDTOs(travels));
    }

    // GET one travel
    @GetMapping("/{id}")
    public ResponseEntity<TravelDTO> getOneTravel(@PathVariable Long id, Principal principal) {

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
        if (!isAuthorizedForTravel(travel, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(travelMapper.toDTO(travel));
    }

    // POST create travel
    @PostMapping
    public ResponseEntity<TravelDTO> createTravel(@RequestBody TravelDTO travelDTO, Principal principal) {
        
        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Travel travel = travelMapper.toDomain(travelDTO);
        travel.setOwnerName(principal.getName());

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
    public ResponseEntity<TravelDTO> updateTravel(@PathVariable Long id, @RequestBody TravelDTO travelDTO, Principal principal) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the travel is empty or exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
 
        // check if the user is the owner (collaborator can not update)
        Travel existingTravel = travelOpt.get();
        if (!existingTravel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // new informartion to update teh travel
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTravel(@PathVariable Long id, Principal principal) {

        // check if is logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // check if the travel is empty or exists
        Optional<Travel> travelOpt = travelService.findById(id);
        if (travelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // check if the user is the owner (collaborator can not delete)
        Travel travel = travelOpt.get();
        if (!travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        travelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // AUXILIARY METHODS
    private boolean isAuthorizedForTravel(Travel travel, String username) {
        if (travel.getOwnerName().equals(username)) {
            return true;
        }
        return travel.getUserTravels() != null && travel.getUserTravels().stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }
}
