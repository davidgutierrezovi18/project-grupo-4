package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.text.Normalizer;
import java.util.regex.Pattern;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.model.Place.Category;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.PlaceRepository;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.DestinationService;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.service.PlaceService;
import es.nextjourney.vs_nextjourney.service.ReviewService;

@Controller
public class ReviewWebController {

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlaceService placeService;

	@Autowired
	private PlaceRepository placeRepository;

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private ImageService imageService;

	private static final Pattern NON_ALNUM_PATTERN = Pattern.compile("[^a-z0-9]");

	@GetMapping("/reviews")
	public String reviews(Model model) {
		return "reviews";
	}

	@GetMapping("/my_reviews")
	public String myReviews(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/sign_in";
		}

		List<Review> userReviews = reviewRepository.findByUserReviewsUsernameOrderByCreatedAtDesc(principal.getName());
		model.addAttribute("reviews", userReviews);
		model.addAttribute("hasReviews", !userReviews.isEmpty());
		return "my_reviews";
	}

	@GetMapping("/my_reviews/{reviewId}/edit")
	public String editMyReviewForm(@PathVariable Long reviewId, Model model, Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(reviewId, principal);
		if (reviewOpt.isEmpty()) {
			return "redirect:/my_reviews";
		}

		Review review = reviewOpt.get();
		Place place = review.getPlace();

		model.addAttribute("formAction", "/my_reviews/" + review.getId() + "/edit");
		model.addAttribute("placeId", place != null && place.getId() != null ? place.getId() : "");
		model.addAttribute("placeName", place != null && place.getName() != null ? place.getName() : "");
		model.addAttribute("placeType", place != null && place.getCategory() != null ? place.getCategory().name() : "");
		model.addAttribute("reviewText", review.getReviewText() != null ? review.getReviewText() : "");
		model.addAttribute("rating1", review.getRating() == 1);
		model.addAttribute("rating2", review.getRating() == 2);
		model.addAttribute("rating3", review.getRating() == 3);
		model.addAttribute("rating4", review.getRating() == 4);
		model.addAttribute("rating5", review.getRating() == 5);
		model.addAttribute("isEditing", true);
		model.addAttribute("existingImages", review.getImages() != null ? review.getImages() : List.of());
		model.addAttribute("hasExistingImages", review.getImages() != null && !review.getImages().isEmpty());
		model.addAttribute("submitLabel", "Guardar cambios");
		model.addAttribute("cancelUrl", "/my_reviews");
		return "add-review";
	}

	@PostMapping("/my_reviews/{reviewId}/edit")
	public String editMyReview(
			@PathVariable Long reviewId,
			Principal principal,
			@RequestParam(name = "rating", defaultValue = "5") int rating,
			@RequestParam(name = "review-text", required = false) String reviewText,
			@RequestParam(name = "deleteImageIds", required = false) List<Long> deleteImageIds,
			@RequestParam(name = "photo", required = false) List<MultipartFile> photos) throws IOException {
		Optional<Review> reviewOpt = getOwnedReview(reviewId, principal);
		if (reviewOpt.isEmpty()) {
			return "redirect:/my_reviews";
		}

		Review review = reviewOpt.get();
		review.setRating(rating);
		review.setReviewText(reviewText);

		if (deleteImageIds != null && review.getImages() != null) {
			review.getImages().removeIf(image -> image.getId() != null && deleteImageIds.contains(image.getId()));
		}

		Review savedReview = reviewService.modifyReview(review);

		if (photos != null) {
			for (MultipartFile photo : photos) {
				if (!photo.isEmpty()) {
					Image image = imageService.createImage(photo);
					image.setReview(savedReview);
					imageService.save(image);
				}
			}
		}

		return "redirect:/my_reviews";
	}

	@PostMapping("/my_reviews/{reviewId}/delete")
	public String deleteMyReview(@PathVariable Long reviewId, Principal principal) {
		Optional<Review> reviewOpt = getOwnedReview(reviewId, principal);
		if (reviewOpt.isEmpty()) {
			return "redirect:/my_reviews";
		}

		reviewService.deleteReview(reviewId);
		return "redirect:/my_reviews";
	}

	@GetMapping("/add-review")
	public String addReview(
			@RequestParam(name = "placeId", required = false) Long placeId,
			@RequestParam(name = "placeName", required = false) String placeName,
			@RequestParam(name = "placeType", required = false) String placeType,
			Model model) {

		model.addAttribute("formAction", "/add-review");
		model.addAttribute("placeId", placeId != null ? placeId : "");
		model.addAttribute("placeName", placeName != null ? placeName : "");
		model.addAttribute("placeType", placeType != null ? placeType : "");
		model.addAttribute("reviewText", "");
		model.addAttribute("rating1", false);
		model.addAttribute("rating2", false);
		model.addAttribute("rating3", false);
		model.addAttribute("rating4", false);
		model.addAttribute("rating5", true);
		model.addAttribute("isEditing", false);
		model.addAttribute("existingImages", List.of());
		model.addAttribute("hasExistingImages", false);
		model.addAttribute("submitLabel", "Publicar reseña");
		model.addAttribute("cancelUrl", "/reviews");
		return "add-review";
	}

	@PostMapping("/add-review")
	public String createReview(
			Principal principal,
			@RequestParam(name = "place-id", required = false) Long placeId,
			@RequestParam(name = "place-name", required = false) String placeName,
			@RequestParam(name = "place-type", required = false) String placeType,
			@RequestParam(name = "rating", defaultValue = "5") int rating,
			@RequestParam(name = "review-text", required = false) String reviewText,
			@RequestParam(name = "photo", required = false) List<MultipartFile> photos) throws IOException {
		Optional<User> userOpt = getAuthenticatedUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/sign_in";
		}
		User user = userOpt.get();

		Optional<Place> placeOpt = resolveOrCreatePlace(placeId, placeName, placeType);
		if (placeOpt.isEmpty()) {
			return "redirect:/reviews";
		}

		Review review = new Review();
		review.setUser(user);
		review.setPlace(placeOpt.get());
		review.setRating(rating);
		review.setReviewText(reviewText);
		review.setCreatedAt(LocalDate.now());
		Review savedReview = reviewService.createReview(review);

		if (photos != null) {
			for (MultipartFile photo : photos) {
				if (!photo.isEmpty()) {
					Image image = imageService.createImage(photo);
					image.setReview(savedReview);
					imageService.save(image);
				}
			}
		}

		return "redirect:/review/" + placeOpt.get().getId();
	}

	@GetMapping("/review/{placeId}")
	public String placeReviewsById(@PathVariable long placeId, Model model) {
		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			model.addAttribute("placeName", "Lugar sin registrar");
			model.addAttribute("reviews", List.of());
			model.addAttribute("hasReviews", false);
			return "place_reviews";
		}

		Place place = placeOpt.get();
		List<Review> reviews = reviewRepository.findByPlaceId(placeId);

		model.addAttribute("place", place);
		model.addAttribute("placeName", place.getName());
		model.addAttribute("reviews", reviews);
		model.addAttribute("hasReviews", !reviews.isEmpty());
		return "place_reviews";
	}

	@GetMapping("/place_reviews")
	public String placeReviewsByName(
			@RequestParam(name = "placeId", required = false) Long placeId,
			@RequestParam(name = "placeName", required = false) String placeName,
			@RequestParam(name = "placeType", required = false) String placeType,
			Model model) {

		String resolvedName = placeName != null && !placeName.isBlank() ? placeName.trim() : "Lugar sin registrar";
		Optional<Place> placeOpt = Optional.empty();

		if (placeId != null) {
			placeOpt = placeService.findById(placeId);
		}

		if (placeOpt.isEmpty() && placeName != null && !placeName.isBlank()) {
			placeOpt = placeRepository.findFirstByNameIgnoreCase(placeName.trim());
		}

		if (placeOpt.isPresent()) {
			Place place = placeOpt.get();
			List<Review> reviews = reviewRepository.findByPlaceId(place.getId());
			model.addAttribute("place", place);
			model.addAttribute("placeName", place.getName());
			model.addAttribute("reviews", reviews);
			model.addAttribute("hasReviews", !reviews.isEmpty());
			return "place_reviews";
		}

		model.addAttribute("placeName", resolvedName);
		model.addAttribute("reviews", List.of());
		model.addAttribute("hasReviews", false);
		model.addAttribute("newPlaceType", placeType != null ? placeType : "");
		return "place_reviews";
	}

	@GetMapping("/api/reviews/place-metrics")
	@ResponseBody
	public Map<String, PlaceMetricResponse> getPlaceMetrics(@RequestParam(name = "names") List<String> names) {
		Map<String, PlaceMetricResponse> response = new HashMap<>();
		List<Place> allPlaces = placeRepository.findAll();
		Map<String, Place> normalizedPlaceMap = new HashMap<>();

		for (Place place : allPlaces) {
			if (place.getName() == null || place.getName().isBlank()) {
				continue;
			}
			normalizedPlaceMap.putIfAbsent(normalizeName(place.getName()), place);
		}

		for (String name : names) {
			if (name == null || name.isBlank()) {
				continue;
			}

			Optional<Place> placeOpt = placeRepository.findFirstByNameIgnoreCase(name.trim());

			if (placeOpt.isEmpty()) {
				Place normalizedMatch = normalizedPlaceMap.get(normalizeName(name));
				if (normalizedMatch != null) {
					placeOpt = Optional.of(normalizedMatch);
				}
			}

			if (placeOpt.isEmpty()) {
				response.put(name, new PlaceMetricResponse(null, 0, 0.0));
				continue;
			}

			Place place = placeOpt.get();
			List<Review> placeReviews = reviewRepository.findByPlaceId(place.getId());
			int totalReviews = placeReviews.size();
			double averageRating = totalReviews == 0
					? 0.0
					: placeReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

			response.put(name, new PlaceMetricResponse(place.getId(), totalReviews, averageRating));
		}

		return response;
	}

	private String normalizeName(String value) {
		String base = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
		String noAccents = Normalizer.normalize(base, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		return NON_ALNUM_PATTERN.matcher(noAccents).replaceAll("");
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

	private Optional<Place> resolveOrCreatePlace(Long placeId, String placeName, String placeType) {
		if (placeId != null) {
			return placeService.findById(placeId);
		}

		if (placeName == null || placeName.isBlank()) {
			return Optional.empty();
		}

		Optional<Place> existingPlace = placeRepository.findFirstByNameIgnoreCase(placeName.trim());
		if (existingPlace.isPresent()) {
			return existingPlace;
		}

		Optional<Destination> destinationOpt = destinationService.findAll().stream().findFirst();

		Place place = new Place();
		place.setName(placeName.trim());
		place.setCategory(mapCategory(placeType));
		place.setDescription(buildGeneratedDescription(placeType));
		destinationOpt.ifPresent(place::setDestination);
		placeService.save(place);
		return Optional.of(place);
	}

	private Category mapCategory(String placeType) {
		String normalizedType = placeType == null ? "" : placeType.trim().toLowerCase();
		if (normalizedType.contains("muse")) {
			return Category.Museo;
		}
		if (normalizedType.contains("park") || normalizedType.contains("parque") || normalizedType.contains("garden")) {
			return Category.Parque;
		}
		if (normalizedType.contains("plaza") || normalizedType.contains("square")) {
			return Category.Plaza;
		}
		if (normalizedType.contains("mirador") || normalizedType.contains("viewpoint")) {
			return Category.Mirador;
		}
		if (normalizedType.contains("historic") || normalizedType.contains("monument") || normalizedType.contains("historia")) {
			return Category.Historia;
		}
		return Category.Otro;
	}

	private String buildGeneratedDescription(String placeType) {
		StringBuilder builder = new StringBuilder("Lugar creado automaticamente desde una reseña");
		if (placeType != null && !placeType.isBlank()) {
			builder.append(". Tipo: ").append(placeType.trim());
		}
		return builder.toString();
	}

	public static class PlaceMetricResponse {
		public Long placeId;
		public int reviewCount;
		public double averageRating;

		public PlaceMetricResponse(Long placeId, int reviewCount, double averageRating) {
			this.placeId = placeId;
			this.reviewCount = reviewCount;
			this.averageRating = averageRating;
		}
	}
}
