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

	@GetMapping("/faq")
	public String faq() {
		return "faq";
	}


	@GetMapping("/add_place")
	public String addPlace() {
		return "add_place";
	}

	@GetMapping("/one_destination")
	public String oneDestination(Model model) {
		return "one_destination";
	}

	@GetMapping("/admin_users")
	public String adminUsers(Model model) {
		return "admin_users";
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
