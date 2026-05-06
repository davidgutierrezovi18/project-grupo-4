package es.nextjourney.vs_nextjourney.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.nextjourney.vs_nextjourney.dto.ReviewDTO;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.PlaceService;
import es.nextjourney.vs_nextjourney.service.ReviewService;
import es.nextjourney.vs_nextjourney.service.ImageService;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

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

	@Autowired
    private ImageService imageService;

	// anyone can see the reviews
	@GetMapping({"", "/"})
    public ResponseEntity<Page<ReviewDTO>> getMyReviews(Principal principal, Pageable pageable) {
        Optional<User> userOpt = getAuthenticatedUser(principal);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<ReviewDTO> reviews = reviewRepository
                .findByUserReviewsUsernameOrderByCreatedAtDesc(principal.getName(), pageable)
                .map(this::toDto);

        return ResponseEntity.ok(reviews);
    }

	// anyone can see the reviews
	@GetMapping("/{id}")
	public ResponseEntity<ReviewDTO> getOneReview(@PathVariable Long id, Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return ResponseEntity.ok(toDto(reviewOpt.get()));
	}

	// anyone can see the reviews
	@GetMapping("/places/{placeId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByPlace(@PathVariable Long placeId, Pageable pageable) {
        Optional<Place> placeOpt = placeService.findById(placeId);
        if (placeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<ReviewDTO> reviews = reviewRepository.findByPlaceId(placeId, pageable)
                .map(this::toDto);

        return ResponseEntity.ok(reviews);
    }

	// only authenticated users and admin can create reviews
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

	// only the review owner and admin can update reviews
	@PutMapping("/{id}")
	public ResponseEntity<ReviewDTO> updateReview(
			@PathVariable Long id,
			@RequestBody ReviewDTO reviewDTO,
			Principal principal, Authentication authentication) {
		
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
				Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Review review = reviewOpt.get();

		boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));
        
		if (!isAdmin && (review.getUser() == null || !review.getUser().getUsername().equals(principal.getName()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

		if (!isValidRating(reviewDTO.rating()) || !isValidReviewText(reviewDTO.reviewText())) {
			return ResponseEntity.badRequest().build();
		}

		review.setRating(reviewDTO.rating());
		review.setReviewText(reviewDTO.reviewText().trim());

		Review savedReview = reviewService.modifyReview(review);
		return ResponseEntity.ok(toDto(savedReview));
	}

	// only the review owner and admin can update reviews
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal, Authentication authentication) {

		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Optional<Review> reviewOpt = getOwnedReview(id, principal);
		if (reviewOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Review review = reviewOpt.get();

		boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));
        
		if (!isAdmin && (review.getUser() == null || !review.getUser().getUsername().equals(principal.getName()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

		reviewService.deleteReview(id);
		return ResponseEntity.noContent().build();
	}

	// only the review owner and admin can upload the review image
	@PostMapping("/{id}/image")
    public ResponseEntity<Object> uploadReviewImage(
            @PathVariable Long id, 
            @RequestParam MultipartFile imageFile, 
            Principal principal, Authentication authentication) throws IOException, SQLException {
        if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

        Optional<Review> reviewOpt = getOwnedReview(id, principal);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

		Review review = reviewOpt.get();

		boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));
        
		if (!isAdmin && (review.getUser() == null || !review.getUser().getUsername().equals(principal.getName()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Image image = new Image();
        image.setImageFile(new SerialBlob(imageFile.getBytes()));
        image.setContentType(imageFile.getContentType());
		image.setReview(review);
        review.setImage(image);
        
        reviewService.modifyReview(review);

        URI location = fromCurrentContextPath()
                .path("/api/v1/images/{imageId}/media")
                .buildAndExpand(image.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

	// only the review owner and admin can delete the review image
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteReviewImage(@PathVariable Long id, Principal principal, Authentication authentication) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

        Optional<Review> reviewOpt = getOwnedReview(id, principal);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

		Review review = reviewOpt.get();

		boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));
        
		if (!isAdmin && (review.getUser() == null || !review.getUser().getUsername().equals(principal.getName()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (review.getImage() != null) {
            review.setImage(null);
            reviewService.modifyReview(review);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
	
	// AUXILIARY METHODS
	private ReviewDTO toDto(Review review) {
		String authorName = "";
		if (review.getUser() != null) {
			authorName = review.getUser().getName() != null ? review.getUser().getName() : "";
			if (authorName.isBlank() && review.getUser().getUsername() != null) {
				authorName = review.getUser().getUsername();
			}
		}

		Long imageId = review.getImage() != null ? review.getImage().getId() : null;
		String imageUrl = imageId != null
				? fromCurrentContextPath().path("/api/v1/images/{imageId}/media").buildAndExpand(imageId).toUriString()
				: null;

		return new ReviewDTO(review.getId(), review.getReviewText(), review.getRating(), authorName, imageId, imageUrl);
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

		return reviewOpt;
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
