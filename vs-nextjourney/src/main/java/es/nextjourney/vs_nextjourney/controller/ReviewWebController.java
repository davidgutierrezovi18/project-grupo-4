package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.model.Place.Category;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.PlaceRepository;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.service.DestinationService;
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

	@ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();

		if (principal != null) {
			model.addAttribute("logged", true);
			model.addAttribute("userName", principal.getName());
			model.addAttribute("admin", request.isUserInRole("ADMIN"));
		} else {
			model.addAttribute("logged", false);
		}
	}

	@GetMapping("/reviews")
	public String reviews(Model model) {
		return "reviews";
	}

	@GetMapping("/my_reviews")
	public String myReviews(Model model) {
		return "my_reviews";
	}

	@GetMapping("/add-review")
	public String addReview(
			@RequestParam(name = "placeId", required = false) Long placeId,
			@RequestParam(name = "placeName", required = false) String placeName,
			@RequestParam(name = "placeType", required = false) String placeType,
			@RequestParam(name = "placeLat", required = false) String placeLat,
			@RequestParam(name = "placeLon", required = false) String placeLon,
			Model model) {

		model.addAttribute("formAction", "/add-review");
		model.addAttribute("placeId", placeId != null ? placeId : "");
		model.addAttribute("placeName", placeName != null ? placeName : "");
		model.addAttribute("placeType", placeType != null ? placeType : "");
		model.addAttribute("placeLat", placeLat != null ? placeLat : "");
		model.addAttribute("placeLon", placeLon != null ? placeLon : "");
		return "add-review";
	}

	@PostMapping("/add-review")
	public String createReview(
			HttpSession session,
			@RequestParam(name = "place-id", required = false) Long placeId,
			@RequestParam(name = "place-name", required = false) String placeName,
			@RequestParam(name = "place-type", required = false) String placeType,
			@RequestParam(name = "place-lat", required = false) String placeLat,
			@RequestParam(name = "place-lon", required = false) String placeLon,
			@RequestParam(name = "rating", defaultValue = "5") int rating,
			@RequestParam(name = "review-text", required = false) String reviewText) {

		// TEMPORAL: allows creating reviews without login for testing.
		// To restore mandatory login, replace with:
		// Optional<User> userOpt = getSessionUser(session);
		// if (userOpt.isEmpty()) { return "redirect:/sign_in"; }
		// User user = userOpt.get();
		User user = getSessionUser(session).orElseGet(this::getOrCreateGuestUser);

		Optional<Place> placeOpt = resolveOrCreatePlace(placeId, placeName, placeType, placeLat, placeLon);
		if (placeOpt.isEmpty()) {
			return "redirect:/reviews";
		}

		Review review = new Review();
		review.setUser(user);
		review.setPlace(placeOpt.get());
		review.setRating(rating);
		review.setReviewText(reviewText);
		review.setCreatedAt(LocalDate.now());
		reviewService.createReview(review);

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
			@RequestParam(name = "placeLat", required = false) String placeLat,
			@RequestParam(name = "placeLon", required = false) String placeLon,
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
		model.addAttribute("newPlaceLat", placeLat != null ? placeLat : "");
		model.addAttribute("newPlaceLon", placeLon != null ? placeLon : "");
		return "place_reviews";
	}

	@GetMapping("/api/reviews/place-metrics")
	@ResponseBody
	public Map<String, PlaceMetricResponse> getPlaceMetrics(@RequestParam(name = "names") List<String> names) {
		Map<String, PlaceMetricResponse> response = new HashMap<>();

		for (String name : names) {
			if (name == null || name.isBlank()) {
				continue;
			}

			Optional<Place> placeOpt = placeRepository.findFirstByNameIgnoreCase(name.trim());
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

	private Optional<User> getSessionUser(HttpSession session) {
		Object userObject = session.getAttribute("currentUser");
		if (userObject instanceof User) {
			return Optional.of((User) userObject);
		}

		Object userId = session.getAttribute("currentUserId");
		if (userId instanceof Long) {
			return userRepository.findById((Long) userId);
		}

		if (userId instanceof Integer) {
			return userRepository.findById(((Integer) userId).longValue());
		}

		return Optional.empty();
	}

	private User getOrCreateGuestUser() {
		// TEMPORAL helper for testing without authentication.
		// Remove this method once login is required again.
		return userRepository.findByUsername("guest").orElseGet(() -> {
			User guest = new User();
			guest.setRol("USER");
			guest.setName("Invitado");
			guest.setLastName("Temporal");
			guest.setUsername("guest");
			guest.setEmail("guest@local.test");
			guest.setPassword("guest");
			return userRepository.save(guest);
		});
	}

	private Optional<Place> resolveOrCreatePlace(Long placeId, String placeName, String placeType, String placeLat, String placeLon) {
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
		place.setDescription(buildGeneratedDescription(placeType, placeLat, placeLon));
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

	private String buildGeneratedDescription(String placeType, String placeLat, String placeLon) {
		StringBuilder builder = new StringBuilder("Lugar creado automaticamente desde una reseña");
		if (placeType != null && !placeType.isBlank()) {
			builder.append(". Tipo: ").append(placeType.trim());
		}
		if (placeLat != null && !placeLat.isBlank() && placeLon != null && !placeLon.isBlank()) {
			builder.append(". Coordenadas: ").append(placeLat.trim()).append(", ").append(placeLon.trim());
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
