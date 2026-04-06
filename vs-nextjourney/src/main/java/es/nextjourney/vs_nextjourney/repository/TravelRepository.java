package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Travel;

import java.util.Optional;


import java.util.List;

public interface TravelRepository extends JpaRepository<Travel, Long> {

    List<Travel> findByOwnerName(String ownerName);

    List<Travel> findByUserTravels_Id(Long userId);

    List<Travel> findByUserTravels_Username(String username);

    Optional<Travel> findByTitle(String title);

}