package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.nextjourney.vs_nextjourney.javaClass.Travel;

public interface TravelRepository extends JpaRepository<Travel, Long> {

}