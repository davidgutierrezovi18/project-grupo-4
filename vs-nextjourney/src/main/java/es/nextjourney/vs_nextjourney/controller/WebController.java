package es.nextjourney.vs_nextjourney.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/index")
	public String home() {
		return "index";
	}

	@GetMapping("/contact")
	public String contact() {
		return "contact";
	}

	@GetMapping("/faq")
	public String faq() {
		return "faq";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/sign_in")
	public String signIn() {
		return "sign_in";
	}

	@GetMapping("/mytravels")
	public String myTravels(Model model) {
		return "mytravels";
	}

	@GetMapping("/reviews")
	public String reviews(Model model) {
		return "reviews";
	}

	@GetMapping("/user_profile")
	public String userProfile(Model model) {
		return "user_profile";
	}

	@GetMapping("/edit_profile")
	public String editProfile(Model model) {
		return "edit_profile";
	}

	@GetMapping("/add_place")
	public String addPlace() {
		return "add_place";
	}

	@GetMapping("/create_new_travel")
	public String createNewTravel() {
		return "create_new_travel";
	}

	@GetMapping("/one_destination")
	public String oneDestination(Model model) {
		return "one_destination";
	}

	@GetMapping("/one_travel")
	public String oneTravel(Model model) {
		return "one_travel";
	}

	@GetMapping("/admin_users")
	public String adminUsers(Model model) {
		return "admin_users";
	}

	@GetMapping("/edit_travel")
	public String editTravel(Model model) {
		return "edit_travel";
	}

	@GetMapping("/add-review")
	public String addReview(Model model) {
		return "add-review";
	}

	@GetMapping("/my_reviews")
	public String myReviews(Model model) {
		return "my_reviews";
	}

	@GetMapping("/place_reviews")
	public String placeReviews(Model model) {
		return "place_reviews";
	}

	@GetMapping("/error403")
	public String showError403(Model model) {
		return "error403";
	}

	@GetMapping("/error404")
	public String showError404(Model model) {
		return "error404";
	}

	@GetMapping("/error500")
	public String showError500(Model model) {
		return "error500";
	}

	
}
