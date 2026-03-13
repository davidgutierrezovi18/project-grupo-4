package es.nextjourney.vs_nextjourney.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	Optional<Place> findFirstByNameIgnoreCase(String name);

}