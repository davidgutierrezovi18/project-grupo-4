package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
