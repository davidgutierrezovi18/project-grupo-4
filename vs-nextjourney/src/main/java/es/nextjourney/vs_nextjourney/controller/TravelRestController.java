package es.nextjourney.vs_nextjourney.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.nextjourney.vs_nextjourney.dto.TravelDTO;
import es.nextjourney.vs_nextjourney.dto.TravelMapper;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.TravelService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/v1/travels")
public class TravelRestController {
    @Autowired
    private TravelService travelService;

    @Autowired
    private TravelMapper travelMapper;

    // GET all travels of the authenticated user
    @GetMapping("/")
    public ResponseEntity<?> getTravels(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario no autenticado");
        }
        String username = principal.getName();
        List<Travel> travels = travelService.findAllByUser(username);
        return ResponseEntity.ok(travelMapper.toDTOs(travels));
    }

    // GET one travel by id (verifies that the user is the owner or collaborator)
    @GetMapping("/{id}")
    public ResponseEntity<?> getTravel(@PathVariable long id, Principal principal) {
        Optional<Travel> travelOptional = travelService.findById(id);
        if (travelOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Travel travel = travelOptional.get();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para acceder a este viaje");
        }
        
        String username = principal.getName();
        boolean isOwner = travel.getOwnerName().equals(username);
        boolean isCollaborator = isUserCollaborator(travel, username);
        
        if (!isOwner && !isCollaborator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para acceder a este viaje");
        }
        
        return ResponseEntity.ok(travelMapper.toDTO(travel));
    }

    // POST - Create travel
    @PostMapping("/")
    public ResponseEntity<?> createTravel(@RequestBody TravelDTO travelDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario no autenticado");
        }
        
        // Validar fechas
        if (travelDTO.startDate() != null && travelDTO.endDate() != null
                && travelDTO.endDate().isBefore(travelDTO.startDate())) {
            return ResponseEntity.badRequest()
                .body("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        Travel travel = travelMapper.toDomain(travelDTO);
        travel.setOwnerName(principal.getName());
        travelService.createTravel(travel);
        return ResponseEntity.status(HttpStatus.CREATED).body(travelMapper.toDTO(travel));
    }

    // PUT - Update travel
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTravel(@PathVariable long id, @RequestBody TravelDTO travelDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario no autenticado");
        }
        
        Optional<Travel> travelOptional = travelService.findById(id);
        if (travelOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Travel existingTravel = travelOptional.get();
        
        // Validar que el usuario sea el propietario
        if (!existingTravel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Solo el propietario puede editar este viaje");
        }
        
        // Validar fechas
        if (travelDTO.startDate() != null && travelDTO.endDate() != null
                && travelDTO.endDate().isBefore(travelDTO.startDate())) {
            return ResponseEntity.badRequest()
                .body("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        Travel travel = travelMapper.toDomain(travelDTO);
        travel.setId(id);
        travel.setOwnerName(existingTravel.getOwnerName());
        travelService.modifyTravel(travel);
        return ResponseEntity.ok(travelMapper.toDTO(travel));
    }

    // DELETE - Delete travel
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravel(@PathVariable long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario no autenticado");
        }
        
        Optional<Travel> travelOptional = travelService.findById(id);
        if (travelOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Travel travel = travelOptional.get();
        
        // Validar que el usuario sea el propietario
        if (!travel.getOwnerName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Solo el propietario puede eliminar este viaje");
        }
        
        travelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // AUXILIARY METHODS
    private boolean isUserCollaborator(Travel travel, String username) {
        // Verificar en la lista de usuarios colaboradores
        if (travel.getUserTravels() != null) {
            return travel.getUserTravels().stream()
                .anyMatch(user -> user.getUsername().equals(username));
        }
        return false;
    }
    
}
