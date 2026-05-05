package es.nextjourney.vs_nextjourney.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.nextjourney.vs_nextjourney.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByPlaceId(Long placeId, Pageable pageable);

	Page<Review> findByUserReviewsUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

	Page<Review> findByUserReviewsIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}
