package es.nextjourney.vs_nextjourney.service;

import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.repository.TravelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TravelService {

    @Autowired
    private TravelRepository travelRepository;

    public void modifyTravel(Travel travel) {
        travelRepository.save(travel);
    }

    public void createTravel(Travel travel) {
        travelRepository.save(travel);
    }

    public List<Travel> findAll() {
        return travelRepository.findAll();
    }

    public Page<Travel> findAllPaginated(Pageable pageable) {
        return travelRepository.findAll(pageable);
    }

    public Optional<Travel> findById(long id) {
        return travelRepository.findById(id);
    }

    public void deleteById(long id) {
        travelRepository.deleteById(id);
    }

    public void save(Travel travel) {
        travelRepository.save(travel);
    }

    public List<Travel> findByOwnerName(String ownerName) {
        return travelRepository.findByOwnerName(ownerName);
    }

    public List<Travel> findByUserId(long userId) {
        return travelRepository.findByUserTravels_Id(userId);
    }

    public Page<Travel> findAllByUser(String username, Pageable pageable) {
        return travelRepository.findByOwnerOrCollaborator(username, pageable);
    }

    public List<Travel> findAllByUser(String username) {
        // Reutilizamos el método paginado pero pidiendo "todo"
        return travelRepository.findByOwnerOrCollaborator(username, Pageable.unpaged()).getContent();
    }

    public void checkUserCanAccess(Travel travel, String username) {
        boolean isOwner = travel.getOwnerName().equals(username);

        boolean isCollaborator = travel.getUserTravels() != null &&
                travel.getUserTravels().stream()
                        .anyMatch(user -> user.getUsername().equals(username));

        if (!isOwner && !isCollaborator) {
            throw new RuntimeException("No tienes permisos para acceder a este viaje");
        }
    }

}
