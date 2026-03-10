package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Destination;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

}