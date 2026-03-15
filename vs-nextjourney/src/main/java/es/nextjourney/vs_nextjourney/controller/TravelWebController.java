package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.TravelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class TravelWebController {
    @Autowired
    private TravelService travelService;

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

    // All the travels of a specific user
    @GetMapping("/mytravels")
    public String myTravels(Model model, Principal principal) {
        String username = principal.getName();
        List<Travel> travels = travelService.findByOwnerName(username);
        model.addAttribute("travels", travels);
        return "mytravels";
    }

    // One travel
    @GetMapping("/travel/{id}")
    public String oneTravel(Model model, @PathVariable long id, Principal principal) {
        Optional<Travel> travel = travelService.findById(id);
        if (travel.isPresent()) {
            if (!travel.get().getOwnerName().equals(principal.getName())) {
                return "error403";
            }
            model.addAttribute("travel", travel.get());
            return "one_travel";
        }
        return "error404";
    }

    // Create travel
    @PostMapping("/travel/new")
    public String newTravel(Model model, Travel travel, HttpSession session) {
        if(session!=null){
            travelService.save(travel);
		    return "create_new_travel";
        }
        else{
            return "sign_in";
        }
	}

    @PostMapping("/travel/new")
    public String newTravel(@ModelAttribute Travel travel, Principal principal) {
        travel.setOwnerName(principal.getName());
        travelService.save(travel);
        return "redirect:/mytravels";
    }

    @PostMapping("/travel/new")
    public String newTravel(@ModelAttribute Travel travel, Principal principal) {
        if (principal == null) {
            return "redirect:/sign_in";
        }

        travel.setOwnerName(principal.getName());

        travelService.save(travel);

        return "redirect:/mytravels";
    }


    // Edit travel
    @GetMapping("/travel/{id}/edit")
    public String editTravelGet(Model model, HttpSession session, @PathVariable long id) {
        if(session!=null){

            Optional<Travel> travel = travelService.findById(id);
            if (travel.isPresent()) {
                model.addAttribute("travel", travel.get());
                return "edit_travel";
            } else {
                return "error404";
            }
        }
        else{
            return "sign_in";
        }
	}

    // Edit travel
    @PostMapping("/travel/{id}/edit")
    public String editTravelPost(Model model, HttpSession session) {
        if(session!=null){
		    return "edit_travel";
        }
        else{
            return "sign_in";
        }
	}

/* 
	@PostMapping("/editbook")
	public String editBookProcess(Model model, Book book, boolean removeImage, MultipartFile imageField)
			throws IOException, SQLException {

		updateImage(book, removeImage, imageField);

		bookService.save(book);

		model.addAttribute("bookId", book.getId());

		return "redirect:/books/" + book.getId();
	}
*/

    // Delete travel
    @PostMapping("/travel/{id}/delete")
	public String deleteTravel(@PathVariable long id) {
		Optional<Travel> travel = travelService.findById(id);
		if (travel.isPresent()) {
			travelService.deleteById(id);
			return "mytravels";
		} else {
			return "error404";
		}
	}
}
