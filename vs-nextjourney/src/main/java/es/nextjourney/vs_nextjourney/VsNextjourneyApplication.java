package es.nextjourney.vs_nextjourney;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.model.Review;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.repository.DestinationRepository;
import es.nextjourney.vs_nextjourney.repository.PlaceRepository;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.repository.TravelRepository;

@SpringBootApplication
public class VsNextjourneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VsNextjourneyApplication.class, args);
	}

	// ADMIN
	@Bean
	CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		// TEMPORALY: delete this block when the default admin already exists in the
		// database
		return args -> {
			if (userRepository.findByUsername("admin").isPresent()) {
				return;
			}

			User admin = new User();
			admin.setName("Administrador");
			admin.setLastName("Sistema");
			admin.setUsername("admin");
			admin.setEmail("admin@nextjourney.local");
			admin.setDateOfBirth(LocalDate.of(2000, 1, 1));
			admin.setPassword(passwordEncoder.encode("grupo4"));
			admin.setRoles(List.of("ADMIN"));

			userRepository.save(admin);

		};
	}

	// USER
	@Bean
	CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		// TEMPORALY: delete this block when the default user already exists in the
		// database
		return args -> {
			if (userRepository.findByUsername("user1").isPresent()) {
				return;
			}

			User user1 = new User();
			user1.setName("user1");
			user1.setLastName("Sistema");
			user1.setUsername("user1");
			user1.setEmail("user1@nextjourney.local");
			user1.setDateOfBirth(LocalDate.of(2000, 1, 1));
			user1.setPassword(passwordEncoder.encode("grupo4"));
			user1.setRoles(List.of("USER"));

			userRepository.save(user1);
		};
	}

	// Destination
	@Bean
	CommandLineRunner createDefaultDestinations(DestinationRepository destinationRepository) {
		return args -> {
			if (destinationRepository.count() > 0) {
				return;
			}

			Image image1 = new Image();
			image1.setContentType("image/jpeg");
			image1.setActive(true);

			Destination paris = new Destination();
			paris.setName("París");
			paris.setDescription("La ciudad del amor, famosa por la Torre Eiffel, el Louvre y su gastronomía.");
			paris.setCountry("Francia");
			paris.setCoverImage(image1);
			destinationRepository.save(paris);

		};
	}
	// Place

	@Bean
	CommandLineRunner createDefaultPlaces(DestinationRepository destinationRepository,
			PlaceRepository placeRepository) {
		return args -> {

			if (placeRepository.count() > 0) {
				return;
			}

			Destination paris = destinationRepository.findByName("París").orElse(null);
			if (paris != null) {
				Place eiffel = new Place();
				eiffel.setName("Torre Eiffel");
				eiffel.setDescription("Icono de París con vistas impresionantes.");
				eiffel.setCategory(Place.Category.Mirador);
				eiffel.setDestination(paris);

				Place louvre = new Place();
				louvre.setName("Museo del Louvre");
				louvre.setDescription("Uno de los museos más importantes del mundo.");
				louvre.setCategory(Place.Category.Museo);
				louvre.setDestination(paris);

				placeRepository.save(eiffel);
				placeRepository.save(louvre);
			}

		};
	}

	// Travel
	@Bean
	CommandLineRunner createDefaultTravels(TravelRepository travelRepository,
			UserRepository userRepository) {
		return args -> {

			if (travelRepository.count() > 0) {
				return;
			}

			User user1 = userRepository.findByUsername("user1").orElse(null);

			Image image1 = new Image();
			image1.setContentType("image/jpeg");
			image1.setActive(true);

			Travel travel1 = new Travel();
			travel1.setOwnerName("user1");
			travel1.setTitle("Viaje a París");
			travel1.setCoverImage(image1);
			travel1.setStartDate(LocalDate.of(2024, 6, 1));
			travel1.setEndDate(LocalDate.of(2024, 6, 7));
			travel1.setDescription("Viaje inolvidable visitando los monumentos más icónicos de París.");
			travel1.setCountries("Francia");
			travel1.setCities("París");
			travel1.setPlaces("Torre Eiffel, Louvre");
			travel1.setRating(5);
			travel1.setComment("Una experiencia increíble.");

			if (user1 != null) {
				travel1.setUserTravels(List.of(user1));
			}

			travelRepository.save(travel1);
		};

	}

	// Review
	@Bean
	CommandLineRunner createDefaultReviews(ReviewRepository reviewRepository,
			UserRepository userRepository,
			PlaceRepository placeRepository,
			DestinationRepository destinationRepository) {
		return args -> {

			if (reviewRepository.count() > 0) {
				return;
			}
			User user1 = userRepository.findByUsername("user1").orElse(null);
			Place eiffel = placeRepository.findFirstByNameIgnoreCase("Torre Eiffel").orElse(null);
			Destination paris = destinationRepository.findByName("París").orElse(null);

			if (user1 == null)
				return;

			Review review1 = new Review();
			review1.setUser(user1);
			review1.setRating(5);
			review1.setReviewText("Impresionante lugar, totalmente recomendable.");
			review1.setCreatedAt(LocalDate.now());
			review1.setPlace(eiffel);
			review1.setDestination(paris);

			reviewRepository.save(review1);
		};
	}

}
