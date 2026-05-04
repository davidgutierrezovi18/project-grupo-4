package es.nextjourney.vs_nextjourney.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.nextjourney.vs_nextjourney.dto.ReviewDTO;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.PlaceService;
import es.nextjourney.vs_nextjourney.service.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewRestController {

	private static final int MAX_REVIEW_LENGTH = 3000;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlaceService placeService;

	@GetMapping({"", "/"})
	public ResponseEntity<List<ReviewDTO>> getMyReviews(Principal principal) {
		Optional<User> userOpt = getAuthenticatedUser(principal);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		List<ReviewDTO> reviews = reviewRepository
				.findByUserReviewsUsernameOrderByCreatedAtDesc(principal.getName())
				.stream()
				.map(this::toDto)
				.toList();

		return ResponseEntity.ok(reviews);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReviewDTO> getOneReview(@PathVariable Long id, Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return ResponseEntity.ok(toDto(reviewOpt.get()));
	}

	@GetMapping("/places/{placeId}")
	public ResponseEntity<List<ReviewDTO>> getReviewsByPlace(@PathVariable Long placeId) {
		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<ReviewDTO> reviews = reviewRepository.findByPlaceId(placeId).stream()
				.map(this::toDto)
				.toList();

		return ResponseEntity.ok(reviews);
	}

	@PostMapping("/places/{placeId}")
	public ResponseEntity<ReviewDTO> createReviewForPlace(
			@PathVariable Long placeId,
			@RequestBody ReviewDTO reviewDTO,
			Principal principal) {
		Optional<User> userOpt = getAuthenticatedUser(principal);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		if (!isValidRating(reviewDTO.rating()) || !isValidReviewText(reviewDTO.reviewText())) {
			return ResponseEntity.badRequest().build();
		}

		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Review review = new Review();
		review.setUser(userOpt.get());
		review.setPlace(placeOpt.get());
		review.setRating(reviewDTO.rating());
		review.setReviewText(reviewDTO.reviewText().trim());
		review.setCreatedAt(LocalDate.now());

		Review savedReview = reviewService.createReview(review);
		return ResponseEntity.created(URI.create("/api/v1/reviews/" + savedReview.getId()))
				.body(toDto(savedReview));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ReviewDTO> updateReview(
			@PathVariable Long id,
			@RequestBody ReviewDTO reviewDTO,
			Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		if (!isValidRating(reviewDTO.rating()) || !isValidReviewText(reviewDTO.reviewText())) {
			return ResponseEntity.badRequest().build();
		}

		Review review = reviewOpt.get();
		review.setRating(reviewDTO.rating());
		review.setReviewText(reviewDTO.reviewText().trim());

		Review savedReview = reviewService.modifyReview(review);
		return ResponseEntity.ok(toDto(savedReview));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		reviewService.deleteReview(id);
		return ResponseEntity.noContent().build();
	}

	private ReviewDTO toDto(Review review) {
		String authorName = "";
		if (review.getUser() != null) {
			authorName = review.getUser().getName() != null ? review.getUser().getName() : "";
			if (authorName.isBlank() && review.getUser().getUsername() != null) {
				authorName = review.getUser().getUsername();
			}
		}

		return new ReviewDTO(review.getId(), review.getReviewText(), review.getRating(), authorName);
	}

	private Optional<User> getAuthenticatedUser(Principal principal) {
		if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
			return Optional.empty();
		}

		return userRepository.findByUsername(principal.getName());
	}

	private Optional<Review> getOwnedReview(Long reviewId, Principal principal) {
		if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
			return Optional.empty();
		}

		Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
		if (reviewOpt.isEmpty()) {
			return Optional.empty();
		}

		Review review = reviewOpt.get();
		if (review.getUser() == null || review.getUser().getUsername() == null) {
			return Optional.empty();
		}

		if (!review.getUser().getUsername().equals(principal.getName())) {
			return Optional.empty();
		}

		return Optional.of(review);
	}

	private boolean isValidRating(int rating) {
		return rating >= 1 && rating <= 5;
	}

	private boolean isValidReviewText(String reviewText) {
		if (reviewText == null) {
			return false;
		}

		String normalized = reviewText.trim();
		return !normalized.isBlank() && normalized.length() <= MAX_REVIEW_LENGTH;
	}
}
