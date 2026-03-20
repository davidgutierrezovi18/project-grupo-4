package es.nextjourney.vs_nextjourney.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByPlaceId(Long placeId);

	List<Review> findByUserReviewsUsernameOrderByCreatedAtDesc(String username);

	List<Review> findByUserReviewsIdOrderByCreatedAtDesc(Long userId);

}
