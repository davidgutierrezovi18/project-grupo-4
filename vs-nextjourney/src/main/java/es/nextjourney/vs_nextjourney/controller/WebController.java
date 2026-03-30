package es.nextjourney.vs_nextjourney.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;


import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.service.DestinationService;
import es.nextjourney.vs_nextjourney.service.ReviewService;
import es.nextjourney.vs_nextjourney.service.TravelService;
import es.nextjourney.vs_nextjourney.service.UserService;

@Controller
public class WebController {

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private ReviewService reviewService;

	private final UserService userService;
	private final TravelService travelService;
	private final ReviewRepository reviewRepository;
	private final PasswordEncoder passwordEncoder;

	public WebController(UserService userService, TravelService travelService, ReviewRepository reviewRepository,
			PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.travelService = travelService;
		this.reviewRepository = reviewRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// Home page
	@GetMapping({"/", "/index"})
	public String home(Model model) {
		List<Destination> randomDestinations = destinationService.getRandomDestinations(3);
		model.addAttribute("random_destinations", randomDestinations);

		List<Review> betterReviews = reviewService.getBetterReviews(3);
		model.addAttribute("better_reviews", betterReviews);

		return "index";
	}

	// FAQ page
	@GetMapping("/faq")
	public String faq() {
		return "faq";
	}

	// Admin pannel
	@GetMapping("/admin_users")
	public String adminUsers(Model model, Principal principal,
			@RequestParam(name = "msg", required = false) String msg) {
		requireAdmin(principal);

		List<User> users = userService.findAll();
		long totalUsers = users.size();
		long adminUsers = users.stream()
			.filter(user -> user.getRoles() != null && user.getRoles().stream()
				.anyMatch(role -> role != null && ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role))))
			.count();

		model.addAttribute("users", users);
		model.addAttribute("hasUsers", !users.isEmpty());
		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("adminUsers", adminUsers);
		model.addAttribute("blockedUsers", users.stream().filter(User::isBlocked).count());
		model.addAttribute("regularUsers", totalUsers - adminUsers);
		model.addAttribute("msg", msg);
		return "admin_users";
	}

	// User detail page - only for admins
	@GetMapping("/admin_users/{id}")
	public String adminUserDetail(@PathVariable long id, Model model, Principal principal,
			@RequestParam(name = "msg", required = false) String msg) {
		requireAdmin(principal);
		User user = userService.findById(id);
		List<Review> userReviews = reviewRepository.findByUserReviewsIdOrderByCreatedAtDesc(id);

		List<Travel> userTravels = new ArrayList<>();
		if (user.getUsername() != null) {
			userTravels.addAll(travelService.findByOwnerName(user.getUsername()));
		}
		userTravels.addAll(travelService.findByUserId(id));

		// Remove duplicates when a travel appears both as owner and collaborator
		Map<Long, Travel> uniqueTravels = userTravels.stream()
				.filter(travel -> travel != null && travel.getId() != null)
				.collect(Collectors.toMap(Travel::getId, travel -> travel, (first, second) -> first));
		List<Travel> mergedTravels = new ArrayList<>(uniqueTravels.values());

		model.addAttribute("user", user);
		model.addAttribute("reviews", userReviews);
		model.addAttribute("travels", mergedTravels);
		model.addAttribute("reviewsCount", userReviews.size());
		model.addAttribute("travelsCount", mergedTravels.size());
		model.addAttribute("hasReviews", !userReviews.isEmpty());
		model.addAttribute("hasTravels", !mergedTravels.isEmpty());
		model.addAttribute("msg", msg);
		return "admin_user_detail";
	}

	// User profile page
	@GetMapping("/admin_users/{id}/profile")
	public String adminUserProfile(@PathVariable long id, Model model, Principal principal) {
		requireAdmin(principal);
		User user = userService.findById(id);
		model.addAttribute("user", user);
		model.addAttribute("adminView", true);
		return "user_profile";
	}

	// Edit user - only for admins
	@PostMapping("/admin_users/{id}/save")
	public String adminSaveUser(@PathVariable long id,
			@RequestParam("name") String name,
			@RequestParam("lastName") String lastName,
			@RequestParam("email") String email,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "isAdmin", defaultValue = "false") boolean isAdmin,
			@RequestParam(value = "blocked", defaultValue = "false") boolean blocked,
			Principal principal) {
		requireAdmin(principal);
		User user = userService.findById(id);

		String currentUser = principal != null ? principal.getName() : "";
		boolean isSelf = user.getUsername() != null && user.getUsername().equals(currentUser);
		if (isSelf) {
			isAdmin = true;
			blocked = false;
		}

		user.setName(name);
		user.setLastName(lastName);
		user.setEmail(email);
		if (newPassword != null && !newPassword.isBlank()) {
			user.setPassword(passwordEncoder.encode(newPassword));
		}
		user.setRoles(isAdmin ? List.of("USER", "ADMIN") : List.of("USER"));
		user.setBlocked(blocked);
		userService.modifyUser(user);

		return "redirect:/admin_users/" + id + "?msg=Usuario actualizado";
	}

	@PostMapping("/admin_users/{id}/toggle-admin")
	public String adminToggleRole(@PathVariable long id, Principal principal) {
		requireAdmin(principal);
		User user = userService.findById(id);

		String currentUser = principal != null ? principal.getName() : "";
		if (user.getUsername() != null && user.getUsername().equals(currentUser)) {
			return "redirect:/admin_users?msg=No puedes quitarte el rol ADMIN";
		}

		if (user.isAdminUser()) {
			user.setRoles(List.of("USER"));
		} else {
			user.setRoles(List.of("USER", "ADMIN"));
		}
		userService.modifyUser(user);
		return "redirect:/admin_users?msg=Rol actualizado";
	}

	@PostMapping("/admin_users/{id}/toggle-block")
	public String adminToggleBlock(@PathVariable long id, Principal principal) {
		requireAdmin(principal);
		User user = userService.findById(id);

		String currentUser = principal != null ? principal.getName() : "";
		if (user.getUsername() != null && user.getUsername().equals(currentUser)) {
			return "redirect:/admin_users?msg=No puedes bloquear tu propia cuenta";
		}

		user.setBlocked(!user.isBlocked());
		userService.modifyUser(user);
		return "redirect:/admin_users?msg=Estado de bloqueo actualizado";
	}

	// 403 error page
	@GetMapping("/error403")
	public String showError403(Model model) {
		return "error403";
	}

	// 404 error page
	@GetMapping("/error404")
	public String showError404(Model model) {
		return "error404";
	}

	// 500 error page
	@GetMapping("/error500")
	public String showError500(Model model) {
		return "error500";
	}

	private void requireAdmin(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso solo para administradores");
		}

		User currentUser = userService.findByUserName(principal.getName());
		if (!currentUser.isAdminUser()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso solo para administradores");
		}
	}

	
}
