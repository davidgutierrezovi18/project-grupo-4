package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;


import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Image;
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
	private final UserDetailsService userDetailsService;

	public WebController(UserService userService, TravelService travelService, ReviewRepository reviewRepository,
			PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
		this.userService = userService;
		this.travelService = travelService;
		this.reviewRepository = reviewRepository;
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
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

	// User profile page
	@GetMapping("/admin_users/{id}/profile")
	public String adminUserProfile(@PathVariable long id, Model model, Principal principal,
			@RequestParam(name = "msg", required = false) String msg) {
		requireAdmin(principal);
		User user = userService.findById(id);
		model.addAttribute("user", user);
		model.addAttribute("adminView", true);
		model.addAttribute("msg", msg);
		return "user_profile";
	}

	@GetMapping("/admin_users/{id}/edit")
	public String adminEditUser(@PathVariable long id, Model model, Principal principal,
			@RequestParam(name = "msg", required = false) String msg) {
		requireAdmin(principal);
		User user = userService.findById(id);
		model.addAttribute("user", user);
		model.addAttribute("adminView", true);
		model.addAttribute("msg", msg);
		return "edit_profile";
	}

	// Edit user - only for admins
	@PostMapping("/admin_users/{id}/edit")
	public String adminSaveUser(@PathVariable long id,
			@RequestParam("name") String name,
			@RequestParam("lastName") String lastName,
			@RequestParam("dateOfBirth") LocalDate dateOfBirth,
			@RequestParam("email") String email,
			@RequestParam("username") String username,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
			@RequestParam(value = "isAdmin", defaultValue = "false") boolean isAdmin,
			@RequestParam(value = "blocked", defaultValue = "false") boolean blocked,
			@RequestParam("imageFile") MultipartFile file,
			Principal principal,
			Model model) throws IOException, SQLException {
		requireAdmin(principal);
		User user = userService.findById(id);
		String currentUser = principal != null ? principal.getName() : "";
		boolean isSelf = user.getUsername() != null && user.getUsername().equals(currentUser);

		if (isSelf) {
			isAdmin = true;
			blocked = false;
		}

		if (!user.getUsername().equals(username) && userService.usernameExists(username)) {
			model.addAttribute("user", user);
			model.addAttribute("adminView", true);
			model.addAttribute("error", "Ese nombre de usuario ya existe");
			return "edit_profile";
		}
		if (!user.getEmail().equals(email) && userService.emailExists(email)) {
			model.addAttribute("user", user);
			model.addAttribute("adminView", true);
			model.addAttribute("error", "Ese correo electrónico ya está en uso");
			return "edit_profile";
		}

		user.setName(name);
		user.setLastName(lastName);
		user.setDateOfBirth(dateOfBirth);
		user.setEmail(email);
		user.setUsername(username);
		if (file != null && !file.isEmpty()) {
			Image image = new Image();
			image.setImageFile(new javax.sql.rowset.serial.SerialBlob(file.getBytes()));
			image.setContentType(file.getContentType());
			user.setImage(image);
		}

		if (newPassword != null && !newPassword.isBlank()) {
			if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
				model.addAttribute("user", user);
				model.addAttribute("adminView", true);
				model.addAttribute("error", "Las contraseñas no coinciden");
				return "edit_profile";
			}
			user.setPassword(passwordEncoder.encode(newPassword));
		}
		user.setRoles(isAdmin ? List.of("USER", "ADMIN") : List.of("USER"));
		user.setBlocked(blocked);
		userService.modifyUser(user);

		if (isSelf) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
			UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
				userDetails,
				userDetails.getPassword(),
				userDetails.getAuthorities()
			);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}

		return "redirect:/admin_users/" + id + "/profile?msg=Usuario actualizado";
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
		return "error/403";
	}

	// 404 error page
	@GetMapping("/error404")
	public String showError404(Model model) {
		return "error/404";
	}

	// 500 error page
	@GetMapping("/error500")
	public String showError500(Model model) {
		return "error/500";
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

	@PostMapping("/admin_users/{id}/delete")
	public String deleteUser(@PathVariable Long id) {
    	userService.deleteById(id);
    	return "redirect:/admin_users";
	}

	
}
 